# Handoff: Store listing graphics

Status: done

When: 2026-08-27
Agent: store listing capture, then orchestrator crop to Play 16:9

APK: `app/build/outputs/apk/debug/app-debug.apk` on `emulator-5554` (AVD `Tablet_10in`). Did not rebuild. Did not change Kotlin.

## Upload set

`store/listing/play/` is what goes in Console. 16:9 / 9:16, 24-bit RGB, no alpha. 1080px+ on the short side.

| Slot | Path | Size |
| --- | --- | --- |
| Icon | `store/listing/icon-512.png` | 512 x 512 RGBA |
| Feature graphic | `store/listing/feature-graphic-1024x500.png` | 1024 x 500 RGB |
| Phone | `store/listing/play/phone/` | 1080 x 1920 / 1920 x 1080 |
| 7-inch | `store/listing/play/tablet-7/` | same as phone |
| 10-inch | `store/listing/play/tablet-10/` | 1440 x 2560 / 2560 x 1440 |

Six shots per slot: portrait/landscape idle dark, athan dark (NOW / DHUHR / Adhan is playing), idle light. No settings sheet. No Jordan clock.

## Sources

Crops in `store/listing/play/` were taken from live emulator hangs, then sized to 16:9 / 9:16. 8:5 source folders were removed 27 Aug 2026. Recapture from the APK if you need a new crop.

## Not shot

No physical tablets. 7-inch geometry was `wm size` on the 10-inch AVD. Athan frames are the UI preview toggle, not a live MP3.
