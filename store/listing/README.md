# Play listing graphics

Upload from `play/`, plus the icon and feature graphic. Do not upload `design/athan-wall-*.png`. Play's tablet slot wants 16:9 and 9:16 (1080px+).

Shot 1 September 2026 evening from the live debug APK. `Tablet_10in` for tablet-10 (1440×2560 / 2560×1440) and for phone + tablet-7 at 1080×1920 / 1920×1080 (`wm size` on the 10-inch AVD; `Tablet_7in` hit System UI ANR). Package `com.mutazyounes.prayerathan`. Albany, 12-hour, horseshoe arc wall, dark mosque only. Weather on the wall. Date without year. No settings sheet.

Athan frames: `adb shell run-as com.mutazyounes.prayerathan am start-foreground-service --user 0 -n com.mutazyounes.prayerathan/.audio.AthanService -a com.mutazyounes.prayerathan.audio.PLAY --es prayer DHUHR` after seeding empty `muted_prayers` in app prefs (defaults mute all five).

No settings sheet in any upload PNG. Light frames were dropped. The live wall is dark only.

## Upload these

| Console field | Folder / file | Pixels | Alpha |
| --- | --- | --- | --- |
| Hi-res icon | `icon-512.png` | 512 x 512 | yes |
| Feature graphic | `feature-graphic-1024x500.png` | 1024 x 500 | no |
| Phone screenshots | `play/phone/*.png` | 1080 x 1920 or 1920 x 1080 | no |
| 7-inch tablet | `play/tablet-7/*.png` | 1080 x 1920 or 1920 x 1080 | no |
| 10-inch tablet | `play/tablet-10/*.png` | 1440 x 2560 or 2560 x 1440 | no |

Phone slot is the 7-inch wall in 9:16, not a phone UI. Console still asks for it. Same four filenames in every folder:

1. `01-portrait-idle-dark.png` — dark wall, Maghrib next, countdown in that cell
2. `02-landscape-idle-dark.png` — same, landscape
3. `03-portrait-athan-dark.png` — Dhuhr cell says Adhan is playing
4. `04-landscape-athan-dark.png` — same, landscape

Max 8 per slot. These 4 fit. Console accepted them 31 Aug 2026.

## Do not upload

- `design/athan-wall-*.png`. Jordan clock and mockup times.
- Settings sheet.
- Light-paper wall frames. The APK is dark only.

## Icon and feature graphic

`icon-512.png` is flattened from `design/candidates/minaret-00-1024.png` (gold stroke in the 66dp safe zone on `#1C1614`). Rebuild with `store/scripts/build-launcher-icons.sh`. Do not use checkerboard-baked exports. 512 x 512 PNG.

`feature-graphic-1024x500.png` is a cover crop of the 10-inch landscape dark idle wall, small icon and the word Athan Clock top left. No Kaaba "official", no price.

## Other listing paste

Copy: `COPY.md`. Data safety answers: `DATA-SAFETY.md`. IARC: `IARC.md`. Privacy URL: https://almutazyounes.github.io/prayerathan-privacy/
