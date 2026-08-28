# PrayerAthan

This file is the spec. If chat history and this file disagree, this file wins. Update it when Mutaz decides something. Do not invent a server, an account system, or a second app.

Mutaz hangs 7-inch and 10-inch Android tablets on the wall in Albany, NY. He already pays for a commercial Athan app and dislikes it. The clock type is too small to read from across the room, and the current time is an afterthought. This project is his wall clock. It shows Albany time, a countdown to the next prayer, today's timetable, and it plays athan when a prayer starts.

It is not a phone super-app. If a feature belongs in Muslim Pro, it does not belong here.

License for our code is MIT unless Mutaz says otherwise. Do not fork MAWAQIT (CC BY-NC-SA) or Al-Azan (AGPL) as the app. Those repos are reference only. MosqueClock and athan-nightstand are MIT and fair to read for kiosk ideas. Prayer math comes from batoulapps adhan-kotlin, not from a web API.

Play Store docs live in `store/`. That folder is not the product spec. The listing is free. Production is not public yet. Athan MP3s are personal-use until rights are cleared. See `audio/SOURCE.md`.

---

## Who it is for

One person. Mutaz. Tablets live on the wall in Albany.

If the tablet moves to a new house, location can change.

---

## Devices

Wall-mounted Android tablets, 7-inch and 10-inch. Always powered, always on. The tablet can hang landscape or portrait. Both orientations are first-class. Follow the device rotation. Do not lock to one axis.

The screen stays awake while the app is in the foreground. `WindowManager` `FLAG_KEEP_SCREEN_ON` and Compose `keepScreenOn` are required. Do not pin the app with lock-task / kiosk in v1. Mutaz should leave the tablet plugged in and turn off battery optimization for this app, or Android will still nap the panel. A sleep after a minute is a bug.

Readable from 8 to 12 feet. If you have to walk up to the tablet to see the minutes, the type is too small.

---

## Screens

Three surfaces. That is the whole product in v1.

**Wall.** The only screen you see when you glance at the tablet. Current weather sits under the city in the header, gold, `22°C  CLEAR`. No Qibla, no Quran, no hamburger. A small gold settings gear sits between the location block and the date. Long-press anywhere still opens settings. Launcher icon is the gold Rub el Hizb from the Albany watermark, not a generic clock.

**Athan playing.** Same wall screen. The countdown block switches to NOW, the prayer name, and the line "Adhan is playing". Albany time stays. The day's list highlights the prayer that just started.

**Settings.** Header gear, or long-press anywhere on the wall. Location is a searchable country list, then a searchable city list. Per-prayer mute lets you mute/unmute individual prayer athans. Theme is Light, Dark, or Auto. Auto is light from sunrise to Maghrib, dark from Maghrib to the next sunrise. Night blackout turns screen totally black and dims brightness between 11 PM and 4 AM with tap-to-wake. Persist choices. No kiosk lock in v1. Sheet background is the `settingsPanel` token, not the wall wash.

---

## Main screen layout

Both orientations ship. The tablet hangs landscape or portrait. Same information on both. Arrangement follows the mockup for that axis. Colors, type, spacing, and pixel layout live in `DESIGN.md`. Do not restyle from this file.

Same content on every hang:

- Location `ALBANY, NY`, current weather under it (`22°C  CLEAR`), and the Gregorian date. No Hijri.
- Albany current time
- Countdown to the next prayer, `HH:MM:SS`
- Today's six times: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. English names only. No Arabic on the cells.

**Portrait.** Albany, then countdown, then the 2-column grid. Live code in `ui/StackedClockWall.kt`.

**Landscape.** Albany and countdown as two columns, prayer row along the bottom.

Next prayer gets a gold outline pill. Past prayers dim. Sunrise is in the grid. It is not a prayer. See behavior.

When athan plays, the countdown block becomes NOW, the prayer name, and "Adhan is playing". Albany stays. Compose should swap that block, not open a second activity.

---

## Visual system

Palette, type scale, hairlines, star motif, and 7-inch vs 10-inch sizing are specified in `DESIGN.md`. Dark is a night mosque wall, umber and bronze, not total black. Light is warm plaster, not white. Wall fill is `design/light-wall-backdrop.png` and `design/dark-wall-backdrop.png`. Ivory clocks, antique brass for next prayer and countdown. No second loud accent. No Hijri. One small gold gear in the header. Not a FAB, not on the prayer grid.

English names, this spelling: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha. No Arabic on the wall. Clock format is 12-hour with AM/PM, matching the mockups. 24-hour can wait as a settings flag.

---

## Design files

Approved mockups, both first-class:

| File | Orientation |
| --- | --- |
| `design/athan-wall-horizontal-v2.png` | Landscape |
| `design/athan-wall-vertical.png` | Portrait |

Older PNGs were deleted on purpose. Do not resurrect them. `DESIGN.md` is the written visual spec over those two images. If a pixel and `DESIGN.md` disagree, fix `DESIGN.md` with Mutaz, do not freelance.

---

## Behavior

Tick every second. Clocks, countdown, and dimming all follow the device clock. Do not refresh once a minute and hope.

**Next prayer** is the next of Fajr, Dhuhr, Asr, Maghrib, Isha. Sunrise is a line in the grid only. No athan at sunrise. After Fajr, the countdown target is Dhuhr even if sunrise has not happened yet. Sunrise still dims once its time has passed.

After Isha, next is tomorrow's Fajr. The countdown may run past midnight. At local date change, recompute the whole timetable for the new day and keep the Fajr target until Fajr fires.

**Timezones.** Albany wall time uses the saved location timezone. Default `America/New_York`. No second city clock on the wall.

**DST.** Use IANA zones. Do not store UTC offsets. Device QA must flip a date across a US DST boundary and confirm Albany, countdown, and timetable all shift.

**Athan.** At Fajr, Dhuhr, Asr, Maghrib, and Isha, start the matching MP3 and switch the countdown block to the playing state. When the file ends, return to idle countdown for the next prayer. If Mutaz taps the screen during athan, stop audio and return to idle. Tapping the settings gear opens the sheet and must not also stop athan. Long-press is still settings, so use a distinct tap, not the same gesture.

**Midnight.** New Gregorian date in the header. New times. Alarms for the new day scheduled. No blank grid and no yesterday's Asr sitting there until he reboots.

**Keep awake.** Screen on while the activity is visible (`FLAG_KEEP_SCREEN_ON` / Compose `keepScreenOn`). Not lock-task. After reboot, the app should come back and reschedule alarms without him opening Settings.

---

## Location and calculation

Library: batoulapps adhan-kotlin, Gradle `com.batoulapps.adhan:adhan2`. MIT. Offline. This is the prayer engine. Aladhan HTTP is not the primary source. Do not require internet after location is stored.

Default location, the house:

- Latitude 42.6526
- Longitude -73.7562
- Timezone `America/New_York`
- Label `Albany, NY`

Calculation: `CalculationMethod.NORTH_AMERICA` (ISNA). That is the Albany default. Do not silently switch to Muslim World League because a sample snippet used it.

Madhab: Shafi. Decided. That is the earlier Asr (shadow factor 1) via adhan-kotlin `Madhab.SHAFI`. HighLatitudeRule stays the library default. Albany is 42.7 N, so high-latitude math almost never fires. Make madhab configurable in settings later. Do not turn the code comments into a fiqh thread.

Location can be set three ways, all persisted on device:

1. These Albany defaults, on first launch.
2. One GPS fix, then save. Do not poll GPS forever on a wall tablet.
3. Searchable country, then searchable city, from the bundled GeoNames list (`assets/cities.tsv`). Picking a city writes the label, coordinates, and timezone. No typed lat/long.

Prayer math is a pure function of coordinates, date, method, and madhab. UI does not compute times. If the designer needs a preview, they call the engine.

Mockup times (Fajr 5:09 AM and so on) are drawn for Thursday 27 August 2026. They are not constants. If adhan-kotlin disagrees with the PNG, the library wins. The PNG is layout.

---

## Audio

Makkah / Maki (الحرم المكي) athan. Sources stay in `audio/`. The APK copies them into `res/raw/`:

- `audio/fajr.mp3` → `athan_fajr.mp3` for Fajr (includes الصلاة خير من النوم)
- `audio/standard.mp3` and extra Haram recordings → selectable Dhuhr, Asr, Maghrib, Isha files. Settings lists them. PLAY is a demo. Gold border is the one that fires at prayer time.

Origins, muezzin names, and license notes are in `audio/SOURCE.md`. Personal use on Mutaz's wall. Replace before strangers install from Play. The recordings are not MIT just because our code is.

Schedule with `AlarmManager.setAlarmClock` so the system treats it as an alarm. Play with `MediaPlayer` on the alarm stream. Hold a short foreground service while audio is playing so the process is not killed mid-athan. Reschedule on `BOOT_COMPLETED`. Exact-alarm permission on current Android is part of the shell work, not optional polish.

Volume should fill a room at living-room distance. Do not duck to media volume that a mute toggle silences. This is the reason he is replacing the commercial app.

Sunrise: silence.

**Athkar.** Short dhikr on the local clock hour: اللهم صل على محمد (`athkar_salawat.mp3`). Files live in `audio/athkar/` with `audio/athkar/SOURCE.md`. Copy into `res/raw/`. Personal use, same rights rule as athan. Not Quran. Play between Fajr and Isha, and only from 8:00 AM through 9:00 PM local. Silent from 10:00 PM until 8:00 AM, even if Fajr was earlier. If an athan is due that minute, athan wins. Tap stops. Settings can turn this off.

---

## Out of scope for v1

Leave these out. If an agent adds one, revert it.

- Qibla
- Quran recitation or verse of the day
- Hijri date
- Widgets, Wear, lock-screen complications
- Accounts, cloud sync, Firebase, a backend
- Multi-mosque or multi-user
- Onboarding carousel, rate-us dialog, splash-screen chrome
- Aladhan API as the timetable
- Forking MAWAQIT or Al-Azan

Settings exist. They are a long-press sheet, not a product pillar.

---

## Stack

One on-device Android app.

- Language: Kotlin
- UI: Jetpack Compose
- Math: `com.batoulapps.adhan:adhan2`
- Alarms: `AlarmManager.setAlarmClock`
- Playback: `MediaPlayer`
- Min SDK 26, targetSdk 36
- No required network after location is saved
- Our code: MIT

Reference, read-only:

- https://github.com/mhdzumair/MosqueClock (MIT, landscape TV layout ideas)
- https://github.com/AIMDaAlien/athan-nightstand (MIT, kiosk)
- https://github.com/batoulapps/adhan-kotlin

There is no API server in this repo and there will not be one for v1.

---

## File map

Single module. Package `com.mutazyounes.prayerathan`. Play Console notes live in `store/`.

```
PrayerAthan/
  AGENTS.md                  <- orchestrator standing orders
  PROJECT.md                 <- this file, product and roster
  DESIGN.md                  <- visual spec
  .cursor/agents/            <- five Cursor subagents
  .cursor/commands/          <- /next, /prayer-engine, …
  .cursor/rules/             <- short glob / always-on rules
  ops/                       <- STATUS, LOG, contracts, handoffs
  store/                     <- Play listing. Free. Production not public.
  design/                    <- athan-wall-horizontal-v2.png, athan-wall-vertical.png
  audio/
    fajr.mp3                 <- Makkah Fajr athan
    standard.mp3             <- Makkah Isha athan, used for Dhuhr–Isha (Ali Mala, 1439)
    SOURCE.md                <- URLs, muezzin, license
  app/src/main/java/com/mutazyounes/prayerathan/
    MainActivity.kt
    PrayerAthanApp.kt
    engine/
      PrayerCalculator.kt    <- adhan-kotlin wrapper
      PrayerDay.kt           <- today's six times + next
      LocationStore.kt       <- defaults, GPS-once, manual
      TimeZones.kt           <- America/New_York, Asia/Amman
    ui/
      WallScreen.kt
      WallTheme.kt           <- colors, type
      PrayerGrid.kt
      AthanPlayingBlock.kt
      SettingsSheet.kt
    audio/
      AthanScheduler.kt      <- AlarmManager
      AthanPlayer.kt         <- MediaPlayer, Fajr vs standard
      AthanService.kt        <- foreground while playing
    shell/
      BootReceiver.kt
      KeepAwake.kt
  app/src/main/res/raw/
    athan_fajr.mp3
    athan_standard.mp3
  app/src/test/java/com/mutazyounes/prayerathan/engine/
    PrayerCalculatorTest.kt
```

Gradle applicationId can match the package. Do not add a `:backend` module.

---

## Agent roster

The parent Cursor agent is the orchestrator. It splits work. It does not throw the whole app at one subagent and hope. Each row below is a real job in this repo.

### Orchestrator

Job. Read `AGENTS.md`, then this file. Spawn `.cursor/agents/` by name. Merge results. Keep this file current when Mutaz decides something.

Owns. Task routing, `PROJECT.md`, `ops/` (status, contracts, handoffs), `.cursor/agents/`, repo hygiene, order of work. Visual pixels belong in `DESIGN.md`.

Must not. Implement the prayer math, the Compose layout, and the alarm path in one sitting "to go faster." Do not overwrite `DESIGN.md`.

Reads first. `AGENTS.md`, `ops/STATUS.md`, this file, then `DESIGN.md`.

Done when. Other agents have clear prompts, files land in the map above, and this spec still matches the product.

### Designer

Job. Compose UI that matches both approved mockups on 7-inch and 10-inch.

Owns. `ui/` : `WallScreen`, theme, type scale, grid, athan-playing block, long-press sheet chrome. Color tokens. Layout that does not fall apart when the countdown hits `00:00:09` or when a prayer name is MAGHRIB. Portrait stack and landscape three-column both have to work.

Must not. Call adhan-kotlin from a composable. Drop one orientation. Add Hijri or weather. Hardcode the mockup's 5:09 AM as the real Fajr. Invent a third layout.

Reads first. `.cursor/agents/designer.md`, this file, `DESIGN.md`, `ops/contracts/ui-api.md`, the two PNGs.

Done when. Wall screen shows engine-provided times, ticks, highlights next, dims past, and swaps the countdown block during athan. Portrait matches the vertical PNG. Landscape matches horizontal-v2.

### Prayer engine

Job. The on-device "backend." Pure Kotlin. Unit-testable with no emulator and no Compose.

Owns. `engine/` : ISNA North America calculation, Shafi Asr default, Albany defaults, Jordan zone, next-prayer + countdown, midnight rollover, persisted location model.

Must not. Compose. AlarmManager. MediaPlayer. Activity flags.

Reads first. `.cursor/agents/prayer-engine.md`, this file, `ops/contracts/engine-api.md`.

Done when. Tests cover a known Albany date, next-prayer after Isha is tomorrow Fajr, sunrise is excluded from athan targets, and Jordan time is `ZonedDateTime` in `Asia/Amman` rather than local plus a magic integer.

### Android shell

Job. The activity that stays on the wall after a reboot.

Owns. `MainActivity`, application class, sensor orientation so landscape and portrait both work, keep-screen-on (`FLAG_KEEP_SCREEN_ON` / Compose `keepScreenOn`), permissions (location once, notifications, exact alarms), `BootReceiver`, Gradle. MP3s already live in `res/raw/`. Lock-task / kiosk is out of v1.

Must not. Reimplement prayer times. Draw its own UI kit. Play MP3s except by starting the audio agent's API.

Reads first. `.cursor/agents/android-shell.md`, this file, then the file map.

Done when. `./gradlew assembleDebug` works, the activity follows landscape or portrait, the screen does not sleep, and boot reschedules whatever the audio agent registered.

### Athan audio

Job. Sound at prayer time, still working after reboot.

Owns. `audio/` : `setAlarmClock`, `MediaPlayer`, Fajr vs standard files from repo `audio/fajr.mp3` and `audio/standard.mp3` (Makkah / Maki, see `audio/SOURCE.md`), foreground service for the duration of the file, stop-on-tap hook the UI can call.

Must not. Layout. CalculationMethod. GPS.

Reads first. `.cursor/agents/athan-audio.md`, this file, `ops/contracts/audio-api.md`, then the engine's next-prayer API.

Done when. At a scheduled time the Fajr file plays for Fajr and the standard file plays for the other four, the wall UI is told athan started and stopped, and a reboot still fires the next one.

### Device QA

Job. Break it on tablet-shaped screens before Mutaz hangs it.

Owns. A checklist, not production code. 7-inch and 10-inch emulators in both portrait and landscape at minimum. Real tablets when available. DST date change, midnight rollover, GPS-denied first launch (defaults to Albany), audio after reboot, keep-screen-on for 10+ minutes, type readable at a distance.

Must not. Redesign. Skip one orientation. Skip audio because the emulator has no speaker story. There is no web app, so no browser QA. Use Android Studio emulator, `adb`, and the physical panels.

Reads first. `.cursor/agents/device-qa.md`, this file, `DESIGN.md`, and the behavior section.

Done when. The checklist is executed and failures are filed as concrete bugs (what screen, what time, what you expected).

---

## Build order

Jobs 1 to 4 are on disk. Device QA on the real wall tablets is still waiting. Play production is a Console gate, not a builder. See `store/README.md`.

1. **Prayer engine.** Calculator, day model, defaults, tests.
2. **Android shell.** Compose activity, both orientations, keep-awake, Gradle, permissions skeleton.
3. **Designer.** Wall UI bound to the engine.
4. **Athan audio.** Schedule, play, tell the UI.
5. **Device QA.** 7-inch, 10-inch, reboot, DST, midnight.

---

## Decided (do not reopen)

Mutaz locked these. Orchestrator standing orders are in `AGENTS.md`.

1. **Madhab.** Shafi (earlier Asr). Hanafi can wait for settings later.
2. **Athan files.** Makkah / Maki. Sources in `audio/`, copies in `res/raw/`. URLs and license in `audio/SOURCE.md`. Personal use on the wall. Replace before strangers install from Play.
3. **Kiosk.** Not in v1. Rely on keep-screen-on. Lock-task is a later settings option if a stray tap dumps him into the launcher.
4. **12-hour vs 24-hour.** 12-hour with AM/PM on the wall. 24-hour is a settings flag only.
5. **Athkar quiet hours.** No hourly athkar from 10:00 PM until 8:00 AM local. Last clip is 9:00 PM. First clip is 8:00 AM, and only if that hour is still between Fajr and Isha.

When he changes one of these, update this file in the same turn as the code. Agents should not have to mine chat logs.
