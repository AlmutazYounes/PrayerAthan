# PrayerAthan

A dedicated, offline, full-screen wall clock and prayer time display for Android tablets (7-inch and 10-inch, landscape and portrait).

Built with Jetpack Compose and [`adhan-kotlin`](https://github.com/batoulapps/adhan-kotlin). Designed for clean visibility across a room.

## Features

- **Large wall clock & next-prayer countdown**: high-contrast typography readable from 8–12 feet.
- **Both orientations supported**: adaptive layouts for portrait and landscape wall mounts.
- **Offline prayer calculation**: accurate on-device prayer times using ISNA calculation method and Shafi madhab via `adhan-kotlin`.
- **Searchable worldwide location catalog**: bundled offline GeoNames database with optional single-fix GPS lookup.
- **Prayer athan audio**: automated Makkah athan playback with distinct Fajr athan, alarm-stream reliability, and tap-to-stop.
- **Hourly athkar**: optional audio dhikr during daytime hours (8:00 AM – 9:00 PM) between Fajr and Isha.
- **Themes**: warm Light, night-mosque Dark, or Auto day/night switching.
- **Keep screen awake**: screen stays on while mounted (`FLAG_KEEP_SCREEN_ON`).
- **No accounts, no tracking, no ads**: 100% on-device operation with optional live weather.

## Tech Stack

- **Platform**: Android (Min SDK 26, Target SDK 36)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Prayer Math**: `com.batoulapps.adhan:adhan2`
- **Audio & Scheduling**: Android `AlarmManager` (`setAlarmClock`) + `MediaPlayer` + Foreground Service

## Building

Clone the repository and build with Gradle:

```bash
./gradlew assembleDebug
```

To run unit tests:

```bash
./gradlew test
```

## License

The code in this repository is licensed under the [MIT License](LICENSE).
