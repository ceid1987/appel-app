import React, {useState} from 'react';
import {
  SafeAreaView,
  View,
  Text,
  TextInput,
  Button,
  StyleSheet,
  PermissionsAndroid,
  Platform,
  NativeModules,
} from 'react-native';

const {BeaconAdvertiser} = NativeModules;

const App = () => {
  // State for UUID, Major, Minor
  const [uuid, setUuid] = useState('2D7A9F0C-E0E8-4CC9-A71B-A21DB2D034A1');
  const [major, setMajor] = useState('100');
  const [minor, setMinor] = useState('1');

  // Whether the beacon is currently advertising
  const [isAdvertising, setIsAdvertising] = useState(false);

  // A message to show status updates (e.g., "Beacon has been stopped")
  const [statusMessage, setStatusMessage] = useState('');

  /**
   * Request necessary BLE permissions on Android 12+.
   */
  const requestBluetoothPermissions = async () => {
    if (Platform.OS === 'android') {
      if (Platform.Version >= 31) {
        const permissions = [
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_ADVERTISE,
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
          PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        ];
        await PermissionsAndroid.requestMultiple(permissions);
      } else {
        await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        );
      }
    }
  };

  /**
   * Start the beacon advertising.
   */
  const startBeacon = async () => {
    setStatusMessage(''); // Clear any old message
    try {
      await requestBluetoothPermissions();

      await BeaconAdvertiser.startAdvertising(
        uuid,
        parseInt(major, 10),
        parseInt(minor, 10),
        true, // Use a pseudo MAC address
      );

      // If successful, update state
      setIsAdvertising(true);
    } catch (error) {
      console.error('Error starting beacon:', error);
      setStatusMessage('Failed to start beacon.');
    }
  };

  /**
   * Stop the beacon advertising.
   */
  const stopBeacon = async () => {
    try {
      await BeaconAdvertiser.stopAdvertising();
      setIsAdvertising(false);
      setStatusMessage('Beacon has been stopped');
    } catch (error) {
      console.error('Error stopping beacon:', error);
      setStatusMessage('Failed to stop beacon.');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* Title */}
      <Text style={styles.title}>STRI Liste Appel</Text>

      {/* Beacon configuration inputs */}
      <View style={styles.inputGroup}>
        <Text>UUID:</Text>
        <TextInput
          style={styles.input}
          value={uuid}
          onChangeText={setUuid}
        />
      </View>
      <View style={styles.inputGroup}>
        <Text>Major:</Text>
        <TextInput
          style={styles.input}
          value={major}
          onChangeText={setMajor}
          keyboardType="numeric"
        />
      </View>
      <View style={styles.inputGroup}>
        <Text>Minor:</Text>
        <TextInput
          style={styles.input}
          value={minor}
          onChangeText={setMinor}
          keyboardType="numeric"
        />
      </View>

      {/* Conditionally render Start/Stop button */}
      {isAdvertising ? (
        <Button title="Stop" onPress={stopBeacon} />
      ) : (
        <Button title="Start" onPress={startBeacon} />
      )}

      {/* Beacon status feedback */}
      <Text style={styles.status}>
        {isAdvertising
          ? 'Beacon is currently advertising'
          : 'Beacon is not advertising'}
      </Text>

      {/* Additional status message (e.g. "Beacon has been stopped") */}
      {statusMessage !== '' && (
        <Text style={styles.statusMessage}>{statusMessage}</Text>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: '600',
    marginBottom: 20,
    textAlign: 'center',
  },
  inputGroup: {
    marginBottom: 12,
  },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 4,
    marginTop: 5,
    padding: 8,
  },
  status: {
    marginTop: 16,
    fontSize: 16,
  },
  statusMessage: {
    marginTop: 8,
    fontStyle: 'italic',
    color: 'blue',
  },
});

export default App;
