# Handoff: Athan audio

Status: done (picker + athkar toggle + per-prayer volume)

When: 2026-09-11
Agent: orchestrator

## What landed

Per-prayer athan volume, 0–100, default 100. Stored in `prayerathan_audio` as `volume_FAJR` etc. `AthanPlayer` applies it with `MediaPlayer.setVolume` on the alarm stream. Mute still skips the alarm.

Settings Prayer Athans card has mute chips plus a gold slider row per prayer. PLAY on a row previews at that volume (`demoId` = `volume:FAJR`). Sound-picker PLAY stays at 100.

Athan selector in settings. PLAY is a demo on the alarm stream. Closing settings stops a demo, not a live prayer athan.

Hourly athkar fires on `:00` between Fajr and Isha, 8:00 AM through 9:00 PM. Silent 10 PM to 8 AM. Single clip: اللهم صل على محمد (Salawat). Settings has On / Off.

## Playback

Selected file at all five prayers. Volume comes from prefs at play time. Athkar still uses full volume. Athkar off cancels the hourly alarms. Athan still wins that minute.

Demo athan does not flip the wall to NOW.
