# Handoff: Athan audio

Status: done (picker + hourly/morning athkar + per-prayer volume + medicine reminder)

When: 2026-09-25
Agent: athan-audio

## What landed

Per-prayer athan volume, 0–100, default 100. Stored in `prayerathan_audio` as `volume_FAJR` etc. `AthanPlayer` applies it with `MediaPlayer.setVolume` on the alarm stream. Mute still skips the alarm.

Settings Prayer Athans card has mute chips plus a gold slider row per prayer. PLAY on a row previews at that volume (`demoId` = `volume:FAJR`). Sound-picker PLAY stays at 100.

Athan selector in settings. PLAY is a demo on the alarm stream. Closing settings stops a demo, not a live prayer athan.

Hourly athkar fires on `:00` between Fajr and Isha, 8:00 AM through 9:00 PM. Silent 10 PM to 8 AM. Single clip: اللهم صل على محمد (Salawat). Settings has On / Off. A medicine slot on that hour skips the athkar alarm.

Morning athkar (issue #8): fixed six-clip sequence at 8:05, 8:10, … 8:30 local. One clip per alarm via `AthkarService` `ACTION_MORNING_PLAY` + `EXTRA_INDEX`. Settings **Morning Athkar** switch, default off (`morning_athkar_enabled`). Yields to athan and medicine. `MorningAthkarScheduler` arms from `AthanController.schedule` / boot. Clips: `athkar_salawat` + `athkar_morning_01`…`05`. Sources in `audio/athkar/SOURCE.md`.

Medicine reminder: optional day/time slots (max 6), Arabic or English voice clips in `res/raw/medicine_*.mp3`. `MedicineScheduler` + `MedicineService` + wall banner. Priority athan > medicine > athkar. Same minute: only medicine. Off until enabled with at least one slot.

Live athan notification is channel `athan_playback_alarm`, `IMPORTANCE_HIGH`, silent, lock-screen public. Full-screen intent opens the wall over the lock. Tap still stops. Volume keys lower the alarm stream and stop at zero. Demos skip full-screen. Playback pins to the built-in speaker so a headset does not also play.

## Playback

Selected file at all five prayers. Volume comes from prefs at play time. Athkar still uses full volume. Athkar off cancels the hourly alarms. Morning off cancels the morning slots. Athan still wins that minute. Medicine cancels athkar if both fire.

## Test

```bash
adb shell am start-foreground-service -n com.mutazyounes.prayerathan/.audio.AthkarService \
  -a com.mutazyounes.prayerathan.audio.MORNING_ATHKAR_PLAY --ei index 0
```
