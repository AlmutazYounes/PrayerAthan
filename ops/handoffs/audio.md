# Handoff: Athan audio

Status: done (picker + athkar toggle + per-prayer volume + medicine reminder)

When: 2026-09-15
Agent: orchestrator

## What landed

Per-prayer athan volume, 0–100, default 100. Stored in `prayerathan_audio` as `volume_FAJR` etc. `AthanPlayer` applies it with `MediaPlayer.setVolume` on the alarm stream. Mute still skips the alarm.

Settings Prayer Athans card has mute chips plus a gold slider row per prayer. PLAY on a row previews at that volume (`demoId` = `volume:FAJR`). Sound-picker PLAY stays at 100.

Athan selector in settings. PLAY is a demo on the alarm stream. Closing settings stops a demo, not a live prayer athan.

Hourly athkar fires on `:00` between Fajr and Isha, 8:00 AM through 9:00 PM. Silent 10 PM to 8 AM. Single clip: اللهم صل على محمد (Salawat). Settings has On / Off.

Medicine reminder: optional day/time slots (max 6), Arabic or English voice clips in `res/raw/medicine_*.mp3`. `MedicineScheduler` + `MedicineService` + wall banner. Priority athan > medicine > athkar. Off until enabled with at least one slot.

Live athan notification is channel `athan_playback_alarm`, `IMPORTANCE_HIGH`, silent, lock-screen public. Full-screen intent opens the wall over the lock. Tap still stops. Volume keys lower the alarm stream and stop at zero. Demos skip full-screen. Playback pins to the built-in speaker so a headset does not also play.

## Playback

Selected file at all five prayers. Volume comes from prefs at play time. Athkar still uses full volume. Athkar off cancels the hourly alarms. Athan still wins that minute. Medicine cancels athkar if both fire.
