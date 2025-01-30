[![fr](https://img.shields.io/badge/readme-français-blue.svg)](https://github.com/ceid1987/appel-app/blob/main/README.pt-fr.md)

# BLE Beacon App

This React Native app allows you to advertise BLE beacons from your mobile phone (Android/iOS).

It generates a pseudo-MAC address from your device's model and serial number. 

Most of the BLE libraries for React Native are deprecated or do not support BLE advertising for both platforms simultaneously, so the beacon functionality is natively coded for each platform. 

# Getting started

## Step 1: Clone the project

```git clone https://github.com/ceid1987/appel-app```

(I recommend using [Android Studio](https://developer.android.com/studio) for testing on android devices)

## Step 2: Install packages

From the root of your project, run:

```npm install```

## Step 3: Build and run app

Android: `npx react-native run-android```

iOS: `npx react-native run-ios`
