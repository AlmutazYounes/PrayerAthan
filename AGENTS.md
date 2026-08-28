# Agent Guidance (PrayerAthan)

This document is for AI coding agents (Claude Code, Cursor, Codex, Copilot, etc.) working on this repository.

## Core Rules

1. **Repo wins over chat:** If instructions in a conversation prompt conflict with committed files (`PROJECT.md`, `DESIGN.md`), follow the repo files or ask for clarification.
2. **Keep scope locked:**
   - **Form factor:** Wall-mounted 7" and 10" Android tablets (landscape and portrait).
   - **No phone super-app bloat:** Do not add Quran readers, Qibla compasses, Hijri calendars, community feeds, accounts, or backends.
   - **No hardcoded mockup times:** Times are dynamic outputs from `adhan-kotlin`. Mockups represent visual layout only.
   - **Keep screen awake:** Always preserve `FLAG_KEEP_SCREEN_ON` in `MainActivity.kt`.
3. **No hallucinated libraries or APIs:**
   - Calculation engine: `com.batoulapps.adhan:adhan2` (offline, pure Kotlin).
   - Audio scheduling: Android `AlarmManager` with `setAlarmClock` + `MediaPlayer` on the alarm audio stream.
4. **Visual tokens over arbitrary colors:**
   - Do not add random hex colors or standard Material palettes. Read and adhere to the tokens defined in `DESIGN.md`.
5. **Quality gates:**
   - Always run `./gradlew test assembleDebug` before committing changes.
   - Verify both portrait and landscape Compose layouts if modifying UI.

---

## Architecture & Responsibilities

| Subsystem | Location | Responsibilities & Boundaries |
| --- | --- | --- |
| **Prayer Engine** | `app/src/main/java/.../engine/` | Pure Kotlin. Handles ISNA prayer calculation, timezones, offline city search, and day models. **No Compose, no Android UI, no MediaPlayer.** |
| **Audio Scheduler** | `app/src/main/java/.../audio/` | Manages exact alarms (`AthanAlarmReceiver`), audio playback (`AthanPlayer`), foreground service (`AthanService`), and hourly Athkar. |
| **Compose UI** | `app/src/main/java/.../ui/` | Renders `WallScreen`, `PrayerGrid`, `SettingsSheet`, and handles theme switching (Light/Dark/Auto). Uses `WallUiState` from `WallViewModel`. |
| **Android Shell** | `app/src/main/java/.../shell/` | Manages `BootReceiver` (rescheduling alarms after reboot), permissions, and `KeepAwake` window flags. |

---

## Important Spec & Doc Files

- `PROJECT.md` — Product specification, design principles, scope boundaries, and behavioral definitions.
- `DESIGN.md` — Complete typography scale, spacing rules, and color token tables for Light and Dark themes.
- `ops/STATUS.md` — Current project state and roadmap items.
- `ops/LOG.md` — Chronological record of decisions and updates.
