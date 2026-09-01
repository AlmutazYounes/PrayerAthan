# Handoff: Device QA

Status: emulator hang checks done. Physical 7-inch and 10-inch still waiting.

When: 2026-08-31
Agent: device-qa

## Emulator

AVD on `emulator-5554`, Android 16 / API 36 Play image. `Tablet_7in` exists on disk and was not started for this pass.

Hang checks that still stand: keep-screen-on, DST, midnight, reboot athan, tap vs long-press, GPS-denied Albany header.

### 2026-08-31 landscape 3×2 hang

App running `com.mutazyounes.prayerathan` on emulator-5554.

- Landscape: **pass**. Two rows of three wider cards (Fajr / Sunrise / Dhuhr, then Asr / Maghrib / Isha). Clock left and Albany weather right sit high. No clipping. Sixth slot filled (Isha), no empty hole. Shot: `ops/shots-landscape-3x2.png` (1920×1080).
- Portrait: **pass**. Classic stack: header, big clock, countdown, 2-col × 3-row grid. Shot: `ops/shots-portrait-check.png` (1080×1920). Rotated back to landscape after.

Ship? Emulator hang for this layout: **yes**. Real tablets still needed before ship.

## Listing crops

Upload set is `store/listing/play/` (16:9 / 9:16). Older 8:5 files under `store/listing/screenshots/` and `store/listing/tablet-7/` / `tablet-10/` were deleted 27 Aug 2026. Recapture from the APK if you need a new crop. Do not upload settings.

Athan on the emulator: `am start-foreground-service` as the package, not as shell, or it dies "Requires permission not exported."

## Ship on real tablets?

No. Hang the APK on Mutaz's 7-inch and 10-inch and fill this table again.
