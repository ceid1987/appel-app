package com.blebeaconproject

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

class BeaconAdvertiserModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising: Boolean = false

    override fun getName(): String {
        return "BeaconAdvertiser"
    }

    /**
     * Called when React Native loads the module. Initialize the Bluetooth adapter here.
     */
    override fun initialize() {
        super.initialize()
        val bluetoothManager = reactApplicationContext.getSystemService(ReactApplicationContext.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth is disabled or not available on this device.")
        } else {
            bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        }
    }

    /**
     * startAdvertising(uuid, major, minor, usePseudoMac, callback)
     */
    @ReactMethod
    fun startAdvertising(uuidString: String, major: Int, minor: Int, usePseudoMac: Boolean, promise: Promise) {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            promise.reject("BLE_ADAPTER_ERROR", "Bluetooth is disabled or not available.")
            return
        }

        // Generate or retrieve your pseudo MAC
        val macAddress = if (usePseudoMac) {
            generatePseudoMacAddress()
        } else {
            // Or, optionally, fetch the device's real MAC if you have that available
            // For privacy reasons, modern Android devices usually do not allow reading the real MAC.
            "00:11:22:33:44:55"
        }

        val uuid: UUID
        try {
            uuid = UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            promise.reject("INVALID_UUID", "Invalid UUID format.")
            return
        }

        try {
            if (isAdvertising) {
                stopAdvertisingInternal()
            }

            // Build advertise settings
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            // Construct the manufacturer data. Format:
            // [16 bytes: UUID] [2 bytes: major] [2 bytes: minor] [6 bytes: MAC address]
            val manufacturerData = ByteBuffer.allocate(16 + 2 + 2 + 6)
            manufacturerData.put(uuid.toByteArray())
            manufacturerData.putShort(major.toShort())
            manufacturerData.putShort(minor.toShort())
            manufacturerData.put(macAddressStringToBytes(macAddress))

            val advertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                // Apple iBeacon is typically 0x004C, but you can adapt as needed
                .addManufacturerData(0x004C, manufacturerData.array())
                .build()

            bluetoothLeAdvertiser?.startAdvertising(settings, advertiseData, advertiseCallback)
            isAdvertising = true

            promise.resolve("Advertising started with UUID=$uuidString, major=$major, minor=$minor, mac=$macAddress")
        } catch (e: SecurityException) {
            promise.reject("BLUETOOTH_PERMISSIONS_ERROR", "Bluetooth permissions are missing or denied.")
        } catch (e: Exception) {
            promise.reject("UNKNOWN_ERROR", e.message)
        }
    }

    /**
     * stopAdvertising(callback)
     */
    @ReactMethod
    fun stopAdvertising(promise: Promise) {
        try {
            if (!isAdvertising) {
                promise.reject("NOT_ADVERTISING", "No advertising in progress.")
                return
            }
            stopAdvertisingInternal()
            promise.resolve("Advertising stopped.")
        } catch (e: Exception) {
            promise.reject("STOP_ERROR", e.message)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertisingInternal() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "BLE advertising started successfully.")
        }

        override fun onStartFailure(errorCode: Int) {
            val message = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "Data too large."
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many advertisers."
                ADVERTISE_FAILED_ALREADY_STARTED -> "Already started."
                ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal error."
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Feature unsupported."
                else -> "Unknown error (code $errorCode)."
            }
            Log.e(TAG, "Advertising onStartFailure: $message")
        }
    }

    /**
     * Generates a pseudo MAC address based on Android ID + brand + model
     */
    private fun generatePseudoMacAddress(): String {
        val androidId = Settings.Secure.getString(reactApplicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        val uniqueDeviceString = androidId + Build.MODEL + Build.BRAND
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(uniqueDeviceString.toByteArray(Charsets.UTF_8))
        // Use first 6 bytes to form a MAC-like string
        return hash.take(6).joinToString(":") { byte -> String.format("%02X", byte) }
    }

    /**
     * Convert MAC string "AA:BB:CC:DD:EE:FF" to ByteArray
     */
    private fun macAddressStringToBytes(macAddress: String): ByteArray {
        return macAddress
            .replace(":", "")
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    /**
     * Extension function for UUID -> ByteArray (16 bytes)
     */
    private fun UUID.toByteArray(): ByteArray {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(this.mostSignificantBits)
        byteBuffer.putLong(this.leastSignificantBits)
        return byteBuffer.array()
    }

    companion object {
        private const val TAG = "BeaconAdvertiserModule"
    }
}
