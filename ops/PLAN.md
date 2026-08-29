# Runbook

This is the orchestrator's execution plan. `ops/STATUS.md` is the live checkbox. This file is what "done" looks like for each job.

Do not skip steps. Do not start Designer on fake times. Engine and shell may overlap. Everything else is serial.

---

## Already done

- Product spec: `PROJECT.md`
- Visual spec: `DESIGN.md` plus the two PNGs
- Athan: `res/raw/athan_saudi.mp3`, `audio/SOURCE.md`
- Contracts: `ops/contracts/`
- Subagents: `.cursor/agents/`
- Slash commands: `/next`, `/prayer-engine`, …

---

## Todo (v1)

1. [x] **Prayer engine** (`prayer-engine`) — 9 tests green after shell
2. [x] **Android shell** (`android-shell`) — assembleDebug pass
3. [x] **Designer** (`designer`) — both layouts, engine-bound
4. [x] **Athan audio** (`athan-audio`) — alarms, Fajr vs standard, boot reschedule
5. [ ] **Device QA** (`device-qa`)
6. [ ] Hang on Mutaz's 7-inch and 10-inch tablets

Play Console state is `store/README.md`, not this file. Production is blocked on Google's 12/14 closed test.

Tick these in `STATUS.md` when the review below passes. Do not tick because the agent said "done."

---

## 1. Prayer engine. Starts first.

**Spawn:** Task `subagent_type: prayer-engine` (or `/prayer-engine`).

**Why first.** Designer and audio both call this API. Without it they invent times.

**Expect on disk**

- `app/src/main/java/com/mutazyounes/prayerathan/engine/` with `PrayerEngine`, `PrayerDay`, `SavedLocation`, `LocationStore`, `TimeZones`
- `app/src/test/java/com/mutazyounes/prayerathan/engine/PrayerCalculatorTest.kt` (or equivalent)
- `ops/handoffs/engine.md` filled in
- `ops/contracts/engine-api.md` still true, or patched in the same change

**Expect from tests (run after shell exists if Gradle is missing)**

- Albany default coords produce six instants for a known date
- After Isha, `nextAthan` is tomorrow Fajr
- Sunrise is never `nextAthan`
- `WallClocks.jordan.zone` is `Asia/Amman`

**I reject if** hardcoded 5:09 AM, Aladhan HTTP, Jordan as local plus hours, Compose in `engine/`.

**Then.** Update STATUS. Spawn shell.

---

## 2. Android shell

**Spawn:** `android-shell`

**Why second.** Nothing compiles until Gradle and an Activity exist. Engine Kotlin may already be on disk. This job wraps it.

**Expect on disk**

- Compiling app, package `com.mutazyounes.prayerathan`, min SDK 26
- `MainActivity` with keep-screen-on
- Both orientations allowed (`sensor`)
- `BootReceiver` stub
- `res/raw/athan_saudi.mp3`
- `./gradlew assembleDebug` exit 0
- `ops/handoffs/shell.md` with assembleDebug pass

Placeholder UI (`Text("PrayerAthan")`) is fine.

**I reject if** a wall layout, a second module called backend, screen can sleep, portrait-only.

**Then.** Run engine unit tests through Gradle. If they fail, send engine back. If they pass, spawn designer.

---

## 3. Designer

**Spawn:** `designer`

**Why third.** Needs `PrayerEngine` to bind. Fake athan-playing via a settings toggle is allowed until audio.

**Expect on disk**

- `ui/WallScreen.kt`, `WallTheme.kt`, `PrayerGrid.kt`, `AthanPlayingBlock.kt`, `SettingsSheet.kt`
- Portrait matches `design/athan-wall-vertical.png` structure
- Landscape matches `design/athan-wall-horizontal-v2.png` structure
- Tokens from `DESIGN.md` only
- Header gear plus long-press settings
- `ops/handoffs/designer.md` with both orientations checked

**I reject if** Hijri, weather, hardcoded mockup times, only one hang, neon gold, adhan-kotlin imported from a composable.

**Then.** Spawn audio.

---

## 4. Athan audio

**Spawn:** `athan-audio`

**Why fourth.** Needs next-prayer instants from the engine and a UI that can show NOW / Adhan is playing.

**Expect on disk**

- `audio/AthanScheduler.kt`, `AthanPlayer.kt`, `AthanService.kt`
- `AlarmManager.setAlarmClock`, not Compose `delay`
- Fajr MP3 at Fajr, standard at the other four, silence at sunrise
- `playback` StateFlow wired to the wall
- Tap stops. Boot reschedules.
- `ops/handoffs/audio.md`

**I reject if** sunrise athan, media-stream ducking that a mute toggle kills, YouTube rips replacing `audio/SOURCE.md` without a note.

**Then.** Spawn QA.

---

## 5. Device QA

**Spawn:** `device-qa`

**Why last.** Needs an APK.

**Expect**

- `ops/handoffs/qa.md` table filled pass/fail
- Bugs written as screen, orientation, time, expected, actual
- Ship? yes or no

**Must run:** 7-inch and 10-inch, both hangs, keep-awake 10+ min, GPS denied (Albany), midnight, a US DST date, athan after reboot, tap vs long-press.

**I reject if** "looks fine" with no table, or audio skipped.

**Then.** If ship is no, spawn the owning agent with the bug list. If yes, Mutaz installs on the wall tablets.

---

## How I run a step

1. Read this file and `STATUS.md`.
2. Spawn one `subagent_type`. Do not rewrite the agent prompt.
3. Read the diff myself. Read the handoff. Run the review bar.
4. Tick STATUS. One line in `LOG.md`.
5. Stop and tell Mutaz what landed, unless he said keep going.

v1 builders 1 to 4 are done. Remaining: physical device QA, then Mutaz's closed testers. Play state is `store/README.md`.
