# Agent Instructions (PrayerAthan)

Welcome to PrayerAthan. This file provides immediate orientation for autonomous coding agents and LLM tools working on this codebase.

## 1. Project Overview

PrayerAthan is a full-screen, dedicated ambient wall clock and prayer time display for Android tablets (7-inch and 10-inch, both landscape and portrait).

- **UI:** 100% Jetpack Compose with adaptive layouts.
- **Prayer Engine:** Pure Kotlin wrapper around `adhan-kotlin` (`com.batoulapps.adhan:adhan2`).
- **Audio:** Reliable background playback with Android `AlarmManager` (`setAlarmClock`), foreground service, and `MediaPlayer`.
- **Target SDK:** 36 | **Min SDK:** 26 (Android 8.0+).

---

## 2. Strict Architectural Boundaries

Agents must respect layer boundaries when adding or modifying code:

1. **`engine/` (Prayer Math & Location)**
   - **Must be pure Kotlin / pure JVM logic.**
   - Do **NOT** import Jetpack Compose, Android Views, or Android audio classes into `engine/`.
   - Always accompany changes in `engine/` with unit tests under `app/src/test/java/.../engine/`.

2. **`audio/` (Alarms & Playback)**
   - Owns `AlarmManager` exact alarm registration, `AthanService` foreground lifecycle, and audio stream configuration.
   - Do **NOT** do layout math or parse raw UI components here.

3. **`ui/` (Jetpack Compose Wall Display)**
   - Follow tokens in `DESIGN.md` (no ad-hoc `#FFxxxx` colors or rogue `MaterialTheme` overrides).
   - Both **Landscape** and **Portrait** orientations are first-class. Every layout change must work on both.
   - Consume UI state via `WallViewModel` and `WallUiState`. Do **NOT** invoke `adhan-kotlin` directly inside composables.

4. **`shell/` (App Host & OS Integration)**
   - Manages `FLAG_KEEP_SCREEN_ON`, system boot listeners (`BootReceiver`), and permissions.

---

## 3. Product Constraints & Non-Negotiables

- **No phone super-app creep:** Do not add Quran readers, Qibla compasses, Hijri calendars, user accounts, analytics, ads, or backends.
- **Offline first:** The app must remain functional with zero internet connection once location is stored. Weather is purely optional and ambient.
- **No hardcoded mockup times:** Fajr, Dhuhr, Asr, Maghrib, and Isha must always be calculated dynamically.
- **Keep Screen Awake:** Never remove or bypass `FLAG_KEEP_SCREEN_ON` in `MainActivity.kt`.

---

## 4. Verification & Validation

Before finalizing any PR or commit, execute:

```bash
# Run all unit tests
./gradlew test

# Verify clean Android build
./gradlew assembleDebug
```

For UI adjustments, inspect the tokens in `DESIGN.md` and reference existing snapshot previews under `store/listing/play/`.
