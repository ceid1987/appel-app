[![en](https://img.shields.io/badge/readme-english-blue.svg)](https://github.com/ceid1987/appel-app)

# Application de Balise BLE

Cette application React Native permet de diffuser des balises BLE (Beacon) depuis votre téléphone mobile (Android/iOS).

Elle génère une pseudo-adresse MAC à partir du modèle de votre appareil et de son numéro de série.

La plupart des bibliothèques BLE pour React Native sont obsolètes ou ne prennent pas en charge la diffusion BLE sur les deux plateformes simultanément. Ainsi, la fonctionnalité de balise a été codée nativement pour chaque plateforme.

# Prise en main

## Étape 1 : Cloner le projet

`git clone https://github.com/ceid1987/appel-app`

(Il est recommandé d’utiliser [Android Studio](https://developer.android.com/studio?hl=fr) pour tester sur des appareils Android.)

## Étape 2 : Installer les dépendances

Depuis la racine de votre projet, exécutez :

`npm install`

## Étape 3 : Compiler et exécuter l’application

Android :

`npx react-native run-android`

iOS :

`npx react-native run-ios`
