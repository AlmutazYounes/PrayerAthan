# Gaps vs a store-ready APK

Facts 27 August 2026.

## Already true

- Package `com.mutazyounes.prayerathan`. Do not change it.
- minSdk 26, targetSdk 36, versionCode 6, versionName `0.6.0`.
- Upload keystore in `keystore/` (gitignored). `signingConfig` release. Signed AAB is on internal testing.
- Keep-screen-on. Both orientations `sensor`.
- On-device ISNA + Shafi. No Aladhan HTTP.
- Location persists. Settings writes city / lat / long / timezone. Use GPS is one fix.
- Listing assets on disk: `store/listing/icon-512.png`, `feature-graphic-1024x500.png`, `store/listing/play/`.
- Privacy page hosted. Console Data safety says nothing collected.

## Still wrong for a public ship

**Audio.** `audio/SOURCE.md`: both MP3s personal use. They ship in `res/raw`. Replace before strangers install.

**Settings.** "Adhan is playing" UI preview does not play the file. Reviewers will toggle it. No privacy URL in the sheet.

**Permissions.** Both `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`. Console still wants FGS and exact-alarm demo videos.

**Version.** Internal test is `0.6.0`.

## Store-ready bar

1. Licensed MP3s in `res/raw`.
2. One exact-alarm permission, declared, with a real demo video.
3. `mediaPlayback` FGS declared, with a real demo video.
4. Privacy link in settings.
5. Preview athan toggle gone from release.
6. Closed testing 12/14, then production apply.
7. `versionName` off `0.1.0` when you mean it.
