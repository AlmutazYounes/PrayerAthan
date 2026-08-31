# Handoff: Android shell

Status: done (GPS-once)

When: 2026-08-27
Agent: android-shell

## What landed

`shell/LocationFixer`. First open with no saved location asks for FINE/COARSE, takes one fix, writes `PrefsLocationStore`, then stops. Settings GPS does the same. Deny or fail on first open writes Albany so the dialog does not repeat.

- `LocationFixer` lives on `PrayerAthanApp`, same store as engine (`PrefsLocationStore`). Not `InMemoryLocationStore`.
- Provider order: platform `fused` (API 31+), else GPS, else NETWORK. No Play Services library.
- API 30+: `getCurrentLocation` (one shot). Older: `requestLocationUpdates` then `removeUpdates` on the first callback. 20s timeout; last-known is a fallback, not a poll.
- Label is the coords (`42.6526, -73.7562`). No geocoder. Timezone is `TimeZone.getDefault().id`. Write goes through `SavedLocation.parse`. Parse fail or deny uses the gold line `Check city, coordinates, and timezone id.` Manual save still works.
- `WallViewModel.useGps()` calls the fixer, then `refresh(..., forceSchedule = true)` on success so `athan.schedule` runs the same way as Save.
- Permission launcher is on `MainActivity`. First open with empty prefs fires it from `WallViewModel`.

Manifest orientation is still `sensor`. INTERNET was already there for weather; this job did not add it.

## assembleDebug

pass

`./gradlew assembleDebug` with `ANDROID_HOME=/Volumes/SamsungT7/Android/LibraryAndroid/sdk`. Exit 0. APK: `app/build/outputs/apk/debug/app-debug.apk`

## What the next agent should know

Device QA: deny location, header stays Albany, no crash, typed city still saves. Grant, one fix, header becomes the coord label, alarms reschedule. Do not expect a city name from GPS.

Designer: leave `useGps()` alone. Settings fields are local `remember` state, so they may look stale until the sheet is closed and opened again. The wall header reads `engine.location()` and updates on the same tick.

Do not start a location listener in `onCreate`. Do not add `ACCESS_BACKGROUND_LOCATION`.

## Contract drift

Did `ops/contracts/` change? no.

## Blockers

None. Device QA can run the GPS-denied Albany path for real now.
