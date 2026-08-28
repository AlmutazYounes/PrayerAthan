# Handoff: Athan audio

Status: done (picker + athkar toggle)

When: 2026-08-27
Agent: athan-audio

## What landed

Athan selector in settings. Four Haram recordings for Dhuhr–Isha, one Fajr file. Gold border is selected. PLAY is a demo on the alarm stream. Closing settings stops a demo, not a live prayer athan.

Hourly athkar fires on `:00` between Fajr and Isha, 8:00 AM through 9:00 PM. Silent 10 PM to 8 AM. Single clip: اللهم صل على محمد (Salawat). Settings has On / Off.

New files: `audio/isha_2009.mp3`, `isha_mullah_2009.mp3`, `friday_2009.mp3` and matching `res/raw/athan_*.mp3`. Notes in `audio/SOURCE.md`.

## Playback

Selected Fajr file at Fajr. Selected standard file at the other four. Athkar off cancels the hourly alarms. Athan still wins that minute.

Demo athan does not flip the wall to NOW.
