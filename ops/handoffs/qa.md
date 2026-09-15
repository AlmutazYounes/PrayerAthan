# Handoff: Device QA

Status: Play listing screenshots recaptured 1 Sept 2026 evening. Physical tablets still waiting.

When: 2026-09-01
Agent: device-qa

## Listing capture (1 Sept 2026)

APK: `app/build/outputs/apk/debug/app-debug.apk` (built same day). Albany defaults, GPS denied, dark wall only. No settings sheet in any PNG.

| Slot | AVD | `wm size` | Notes |
| --- | --- | --- | --- |
| `play/tablet-10/` | `Tablet_10in` | 1440×2560 / 2560×1440 | All four frames pass |
| `play/tablet-7/` | `Tablet_10in` | 1080×1920 / 1920×1080 | 7in dims on 10in AVD (see blocker) |
| `play/phone/` | same as tablet-7 | 1080×1920 / 1920×1080 | Copied from tablet-7 set |

Athan frames: `adb shell run-as com.mutazyounes.prayerathan am start-foreground-service --user 0 -n com.mutazyounes.prayerathan/.audio.AthanService -a com.mutazyounes.prayerathan.audio.PLAY --es prayer DHUHR` after seeding `shared_prefs/prayerathan_audio.xml` with empty `muted_prayers` (defaults mute all five). Landscape athan: start in portrait, rotate, then capture.

Feature graphic: cover crop of tablet-10 `02-landscape-idle-dark.png` plus `icon-512.png` and label "Athan Clock" top-left. 1024×500 RGB.

### Verified UI

- Portrait idle: horseshoe arc, countdown in notch, 2-col prayer grid, Albany header, weather on.
- Landscape idle: arc/clock left ~54%, spaced prayer list right, no vertical divider.
- Athan portrait/landscape: NOW / DHUHR / Adhan is playing, Dhuhr row highlighted.

### Blocker: `Tablet_7in`

`Tablet_7in` hit System UI ANR, splash-only frames, and taskbar tutorial overlays on recapture. `tablet-7/` and `phone/` were shot on `Tablet_10in` at 7-inch Play dimensions instead. Dimensions verified; UI matches 7-inch layout at 1080×1920.

## Emulator hang checks (31 Aug, still stand)

`Tablet_7in` on `emulator-5554`, API 36 Play. Keep-screen-on, DST, midnight, reboot athan, tap vs long-press, GPS-denied Albany header not re-run this pass.

### 2026-08-31 landscape 3×2 hang

- Landscape stacked wall: **pass** (superseded by Concept B arc layout in Sept shots).
- Portrait stacked wall: **pass**.

## Ship on real tablets?

No. Hang the APK on Mutaz's 7-inch and 10-inch and fill this table again before production.

Ship listing PNGs for Console upload? **Yes** (emulator capture only; note tablet-7 AVD workaround above).
