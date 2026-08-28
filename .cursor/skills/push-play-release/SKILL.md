---
name: push-play-release
description: >-
  Bumps versionCode, builds a signed AAB, publishes it to Google Play internal
  testing, and patches store docs. Use when Mutaz says push, ship to Play,
  upload a release, new internal testing build, or /push.
---

# Push Play release

Internal testing only. Not production. Package `com.mutazyounes.prayerathan`. AAB only.

Canonical IDs and tester notes: `store/PUSH.md`. Do not invent a second walk.

## Do this

From repo root, run the scripts. Do not rewrite Console clicks in a fresh heredoc unless a script fails.

```
./store/scripts/push-release.sh "one line of what changed"
```

That bumps if Gradle is not ahead of `store/README.md`, builds `bundleRelease`, publishes in Play Console via ego-browser, then patches docs.

Need a signed AAB and no Console upload:

```
./store/scripts/bump-version.sh
./store/scripts/build-aab.sh
```

Already bumped in Gradle, Console only:

```
./store/scripts/push-release.sh --no-bump "notes"
```

## After the script

1. Confirm Console **Latest release** matches Gradle `versionCode` / `versionName`.
2. Tell Mutaz the opt-in URL: https://play.google.com/apps/internaltest/4701680380313434468
3. Remind him Play lag is minutes, debug APK blocks Update.

If ego-browser hits 2FA or "user is controlling", stop. Hand the browser back. Do not `takeOverTaskSpace`.

## Do not

- Upload an APK
- Apply for production
- Sideload debug over a Play install
- Claim the public store page is live
- Change `applicationId`

## If it breaks

`store/PUSH.md` table. Common: missing `keystore/`, same `versionCode`, debug APK still on the tablet, Console not signed in as mutazyounes@gmail.com.
