# Handoff: Device QA

Status: emulator hang checks done. Physical 7-inch and 10-inch still waiting.

When: 2026-08-27
Agent: device-qa

## Emulator

AVD `Tablet_10in`, Android 16 / API 36 Play image. `Tablet_7in` exists on disk and was not started. 7-inch frames used `wm size` on the 10-inch AVD.

Hang checks that still stand: keep-screen-on, DST, midnight, reboot athan, tap vs long-press, GPS-denied Albany header.

Shots from that pass: `ops/qa-shots/`.

## Listing crops

Upload set is `store/listing/play/` (16:9 / 9:16). Older 8:5 files under `store/listing/screenshots/` and `store/listing/tablet-7/` / `tablet-10/` were deleted 27 Aug 2026. Recapture from the APK if you need a new crop. Do not upload settings.

Athan on the emulator: `am start-foreground-service` as the package, not as shell, or it dies "Requires permission not exported."

## Ship on real tablets?

No. Hang the APK on Mutaz's 7-inch and 10-inch and fill this table again.
