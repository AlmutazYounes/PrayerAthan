# UI API

Designer owns Compose under `ui/`. A ViewModel (shell or `ui/WallViewModel.kt`) is the only thing that talks to engine and audio.

## State the wall screen reads

```kotlin
data class WallUiState(
    val locationLabel: String,      // saved label, all-caps, header
    val locationCity: String,       // saved label, settings pickers
    val locationLatitude: String,
    val locationLongitude: String,
    val locationTimeZoneId: String,
    val locationError: String?,     // gold line after a failed parse
    val gregorianDate: String,      // "27 August"
    val weekday: String,            // "Thursday"
    val albanyTime: String,         // "3:17" plus amPm
    val albanyAmPm: String,
    val jordanTime: String,
    val jordanAmPm: String,
    val nextLabel: String,          // "NEXT ASR" or "NOW"
    val countdown: String,          // "01:24:18" unused while playing
    val athanPlaying: Boolean,
    val playingName: PrayerName?,
    val athkarPlaying: Boolean,     // hourly dhikr, never with athan
    val athkarCaption: String,      // gold Arabic while a clip plays
    val cells: List<PrayerCellState>,
    val twelveHour: Boolean,        // v1 always true
    val themeMode: ThemeMode,       // LIGHT, DARK, AUTO
    val darkTheme: Boolean,         // resolved palette. AUTO uses sunrise/Maghrib.
    val athanSoundId: String,
    val athkarEnabled: Boolean,
    val mutedPrayers: Set<PrayerName>,
    val demoId: String?,
    val nightBlackoutEnabled: Boolean,
    val isNightBlackout: Boolean,   // true from 11 PM to 4 AM unless athan playing
)

enum class ThemeMode { LIGHT, DARK, AUTO }

data class PrayerCellState(
    val name: PrayerName,
    val english: String,            // FAJR, SUNRISE, ...
    val time: String,               // "5:09"
    val kind: CellKind,             // PAST, NEXT, LATER
    val muted: Boolean = false,
)

enum class CellKind { PAST, NEXT, LATER }
```

Tick: ViewModel refreshes `WallUiState` every second from `PrayerEngine` and `AthanController.playback`.

Settings location: searchable country, then searchable city from bundled GeoNames. Picking a city calls `SavedLocation.parse` then `LocationStore.write`. Null parse shows `locationError`. Reset writes `SavedLocation.albany`. After a successful write, refresh immediately and force `athan.schedule` even if the local date did not change. Header `locationLabel` is the saved label, all-caps. Do not import adhan-kotlin in a composable.

## Gestures

- Tap while `athanPlaying`: `AthanController.stop()`.
- Header gear: settings sheet. Child click consumes the tap so it does not also stop athan.
- Long-press: settings sheet. Not the same as tap.

## Layout

Portrait: `design/athan-wall-vertical.png` plus `DESIGN.md`.
Landscape: `design/athan-wall-horizontal-v2.png` plus `DESIGN.md`.

Tokens from `DESIGN.md` only.
