# Tether

**Draw a boundary. Invite your people. Know when someone leaves.**

Tether is a native Android app for private groups (family, hostel mates, travel groups). A group draws a circular **fence** on a map, shares a 6-character invite code, and every member is alerted when someone leaves the fence. It reports boundary crossings only. No live location, no route history.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase&logoColor=black)


---

## Features

- **Circles with a fence:** pick a center on the map, set a radius (100 m to 2 km), name the circle.
- **Invite codes:** 6-character code, copy or share, join in seconds.
- **Live member status:** Inside, Outside, or Unknown for every member.
- **Exit alerts:** other members are notified when someone leaves the fence; tapping the alert opens that circle.
- **Background detection:** geofences are monitored by Google Play Services, so exits are detected with the app closed.
- **Guided permissions:** location, then background location, then notifications.
- **Dark, map-first UI** built with Material 3.

## How it works

```
 Member A's phone                Play Services             Cloud Firestore            Member B's phone
 ----------------                -------------             ---------------            ----------------
 register fence  -------------->
                 <-------------- EXIT transition
 batch write: event + status ------------------------->
                                                         snapshot listener ---------> alert shown:
                                                                                      "A left Home"
```

## Privacy by design

- The device **never uploads coordinates**. Only the fence definition (set by the circle creator) and discrete `EXIT` / `ENTER` events with a timestamp and user id are stored.
- Boundary detection happens on the phone through the platform Geofencing API, which is also battery-friendly: no continuous location updates and no foreground service.
- Anonymous sign-in with a display name. No email, phone number, or account needed.

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Architecture | MVVM, unidirectional data flow (StateFlow), repository pattern |
| DI | Lightweight manual `AppContainer` |
| Backend | Firebase Anonymous Auth, Cloud Firestore |
| Maps / location | Maps Compose, Play Services Location (`GeofencingClient`) |
| Navigation | Navigation Compose |
| Min / compile SDK | 26 / 36 |

## Project structure

```
com.tether.app
  ui/           theme, shared components, one package per screen (Screen + ViewModel + UiState)
  data/         models and TetherRepository (Firestore)
  geofence/     GeofenceManager, GeofenceReceiver
  alerts/       ExitAlertWatcher, NotificationHelper
  util/         invite code, helpers
```

## Getting started

**Prerequisites:** Android Studio (latest stable), a device or emulator with Google Play services.

1. **Clone**
   ```bash
   git clone https://github.com/rudraoberoi10/tether
   ```
2. **Maps key:** create a key with *Maps SDK for Android* enabled and add it to `local.properties` (this file is git-ignored):
   ```properties
   MAPS_API_KEY=your_key_here
   ```
3. **Firebase:** create a project, add an Android app with package `com.tether.app`, enable **Anonymous** sign-in, create a **Firestore** database, apply `firestore.rules`, and download `google-services.json` into `app/`. A template is provided at `app/google-services.json.example`.
4. **Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or press Run in Android Studio.

## Testing with two devices

1. **Device A:** open Tether, grant permissions, set a name, and create a circle with a 300 m fence. Note the invite code.
2. **Device B** (emulator works): set a different name and join with the code. Device A now shows B as *Inside*.
3. On the emulator open **Extended controls, then Location**, set a point about 2 km outside the fence, and click **Set location**.
4. B's exit is detected, written to Firestore, and Device A receives the alert. Move B back inside to see the status return to *Inside* without an alert.

## Design decisions and trade-offs

- **Client-side writes for the MVP.** Creating and joining circles are direct Firestore writes protected by rules for signed-in users. This was the fastest path to a working end-to-end flow, but it is weaker than server-enforced membership.
- **Alerts use a Firestore listener plus a local notification.** This works while the app process is alive (foreground or background), not after the OS kills it.
- **Geofence latency.** Play Services batches transitions to save battery, so an exit can take up to a minute or two to be detected.
- **Anonymous identity.** Reinstalling the app creates a new identity.

## Roadmap

- [ ] Cloud Function (`onDocumentCreated`) plus FCM so alerts arrive even when the app is closed
- [ ] Move create / join / leave to callable Cloud Functions with server-side limits
- [ ] Editable fences and multiple fences per circle
- [ ] Enter alerts and quiet hours
- [ ] Google sign-in and member removal by the owner

## Author

Built by **Rudra Oberoi** 
