# Handoff: Store listing graphics

Status: done (recaptured 1 Sept 2026)

When: 2026-09-01
Agent: device-qa

APK: `app/build/outputs/apk/debug/app-debug.apk` on `Tablet_10in` (`emulator-5554`). `Tablet_7in` attempted; ANR/splash blocked clean frames. `tablet-7/` and `phone/` used `Tablet_10in` at 1080×1920 / 1920×1080.

## Upload set

`store/listing/play/` plus icon and feature graphic. 16:9 / 9:16, 24-bit RGB, no alpha.

| Slot | Path | Size | AVD |
| --- | --- | --- | --- |
| Icon | `store/listing/icon-512.png` | 512×512 RGBA | (unchanged) |
| Feature graphic | `store/listing/feature-graphic-1024x500.png` | 1024×500 RGB | crop from tablet-10 landscape idle |
| Phone | `store/listing/play/phone/` | 1080×1920 / 1920×1080 | `Tablet_10in` @ 7in `wm size` |
| 7-inch | `store/listing/play/tablet-7/` | same | `Tablet_10in` @ 7in `wm size` |
| 10-inch | `store/listing/play/tablet-10/` | 1440×2560 / 2560×1440 | `Tablet_10in` |

Four files per folder (dark only):

1. `01-portrait-idle-dark.png` — arc + 2-col grid, next prayer countdown
2. `02-landscape-idle-dark.png` — arc left, prayer list right
3. `03-portrait-athan-dark.png` — NOW / DHUHR / Adhan is playing
4. `04-landscape-athan-dark.png` — same, landscape

No settings sheet. Athan via foreground service + unmuted prefs seed (see `ops/handoffs/qa.md`).

## Feature graphic

Refreshed from tablet-10 landscape idle. Top-left: `icon-512.png` + "Athan Clock" (not "Wall"). 1024×500 cover crop.

## Not shot

No physical tablets. Internal Play build 28 not installed; debug APK matched current UI (Sept 2026 stacked arc wall).
