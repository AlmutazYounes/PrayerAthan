# How to push PrayerAthan to Play

This is the repeatable upload. Live version numbers live in `store/README.md` and `app/build.gradle.kts`. Account signup, listings, IARC, and leftover declarations live in `PLAY-CONSOLE.md`.

"Push" means a new signed Android App Bundle on **internal testing**. It does not mean the public store. Production is locked until 12 closed testers stay opted in for 14 days. Internal testers do not count.

Do not upload an APK. Play rejects it for this package.

---

## What you are shipping to

| Item | Value |
| --- | --- |
| Google account | mutazyounes@gmail.com |
| Developer | YounesM, personal |
| Developer ID | `5934139594166642747` |
| App ID | `4974554092638251166` |
| Package | `com.mutazyounes.prayerathan` |
| Track | Internal testing |
| Track ID | `4701680380313434468` |
| Testers | `mutazyounes@gmail.com`, `mohtazscape@gmail.com` |
| Opt-in | https://play.google.com/apps/internaltest/4701680380313434468 |
| Console track | https://play.google.com/console/u/0/developers/5934139594166642747/app/4974554092638251166/tracks/internal-testing |
| Prepare URL pattern | `.../tracks/4701680380313434468/releases/<N>/prepare` |

Play App Signing is on. You sign with the **upload** key. Google signs what testers install.

---

## Files that must exist on this machine

Gitignored. Back them up off this laptop.

`keystore/upload.jks` and `keystore/keystore.properties`. Gradle reads the properties file and signs `bundleRelease`. Lose the jks and request an upload-key reset in Console. Annoying, not fatal.

`keystore.properties` keys:

```
storeFile=keystore/upload.jks
storePassword=...
keyAlias=...
keyPassword=...
```

`play/service-account.json` is optional. If it is missing, use Console (the path we actually use). If it is present, `./gradlew publishReleaseBundle` can upload a draft. Setup for that key is `play/README.md`. Mutaz has to grant the service account in Console. Nobody else can.

---

## Bump the version first

Play rejects a second upload at the same `versionCode`.

In `app/build.gradle.kts` `defaultConfig`:

```
versionCode = N
versionName = "0.N.0"
```

`versionCode` is the integer Play uses. Bump it by 1 every push. `versionName` is the label testers see. Keep them in lockstep unless you have a reason not to.

Last shipped numbers are in `store/README.md`. If Gradle is ahead of Console, you already bumped and still need to upload. If they match, bump before building.

---

## Build the signed bundle

From the repo root:

```
./gradlew bundleRelease
```

Output:

```
app/build/outputs/bundle/release/app-release.aab
```

That path is always the same. The AAB is gitignored.

If Gradle says UP-TO-DATE and you just changed Kotlin, do not trust it blindly. Confirm no `app/src/main` files are newer than the AAB. If they are, run `./gradlew bundleRelease` again. A clean rebuild is `./gradlew clean bundleRelease` when something smells off.

`assembleDebug` is the USB APK. It cannot go to Play. It also cannot update a Play install of the same package, and Play cannot update it. Pick one signature per tablet.

---

## Upload. This is the path that actually ships today

There is no service account on disk yet. Open Console in a browser signed in as mutazyounes@gmail.com.

1. Open the internal testing URL above. Confirm **Latest release** is the old version.
2. **Create new release**. Console opens `releases/<next>/prepare`.
3. Drop `app-release.aab` on **Upload app bundles**, or use the Upload button (`input[accept=".aab"]`).
4. Wait until the table shows `App bundle` and `N (0.N.0)`. Processing is often 30 to 90 seconds. A spinner that says the upload is being optimised is normal. Do not click Next while that is still running.
5. Leave **Previous release** as **Not included**. You want testers on this AAB only, not 0.1.0 sitting next to it.
6. Release name can stay the suggested `N (0.N.0)`.
7. Release notes, en-GB, inside the language tags:

```
<en-GB>
Short what changed.
</en-GB>
```

8. **Next**. Review page. Warnings we have seen and ignored:
   - No R8 mapping file. Minify is off. Fine.
   - Native debug symbols missing. A dependency pulled `.so` files. Fine for now.
   - "Upload your app bundle again to apply enhancement changes" is Play App Signing / automatic protection nag. Ignore unless you turned that on on purpose.
9. **Save and publish**. Confirm in the dialog. Internal testing publishes immediately. No Google review.
10. Track page should say **Latest release: N (0.N.0)** and **Available to internal testers**.

If you stop at **Save as draft**, testers keep the old build. Draft is not a push.

Agent uploads have used the same clicks. Chrome must already be logged into Play Console as Mutaz. If Google asks for 2FA, stop and hand the browser back.

---

## Gradle upload, when the service account exists

One-time: follow `play/README.md`, then drop the JSON at `play/service-account.json`.

```
./gradlew bundleRelease publishReleaseBundle
```

The `play {}` block in `app/build.gradle.kts` targets track `internal` and app bundles only.

gradle-play-publisher defaults to a **draft**. Testers will not see it until someone opens the draft in Console and clicks **Save and publish**. Do not tell Mutaz it is live because Gradle exited 0.

To make that task actually roll out, set `releaseStatus` in the `play {}` block. Do not change that without saying so in this file.

---

## After Console says it is live

Play Store on the tablet is slow. Five minutes is common. An hour happens.

On a tablet that already joined internal testing:

1. Same Google account as the tester list.
2. Play Store → profile → Manage apps & device → Updates available.
3. Or search `PrayerAthan`. Listing may still say `com.mutazyounes.prayerathan (unreviewed)`.
4. Or open the opt-in link on the tablet and tap Update / Download.

If Update never appears:

- Debug APK is still installed. Uninstall PrayerAthan, then install from the opt-in link.
- Tablet is on a different Gmail. The opt-in URL 404s or says sorry for non-testers.
- Play has not finished processing. Wait, then pull to refresh.

Do not sideload a new debug APK over a Play install. Uninstall first if you need USB for a day.

---

## Patch the repo after a successful push

Chat is not the spec.

1. `store/README.md`. Internal testing row.
2. `ops/STATUS.md`. The Now paragraph.
3. `ops/LOG.md`. One line at the top.
4. `ops/handoffs/store.md`.
5. `store/CHECKLIST.md`, `store/PLAN.md`, `store/APP-GAPS.md`, `store/PLAY-CONSOLE.md` version mentions.

Leave production wording alone. A push does not make the public URL real.

---

## What this push does not do

- Public listing. Still a gray stub until Google reviews a production apply.
- Closed testing. Different track. Needed for the 12/14 gate.
- Listing copy, screenshots, IARC, Data safety. Those are already in Console. Do not redo them for a binary bump.
- Licensed MP3s. `audio/SOURCE.md` still says personal use. Internal testers are Mutaz and one other Gmail. Do not treat that as a public ship.

---

## When it breaks

| Symptom | Likely cause |
| --- | --- |
| Play rejects APK | Must be `.aab` |
| Version code already used | Forgot to bump `versionCode` |
| Signing failed / unsigned | Missing `keystore/` files |
| Publish task auth error | No `play/service-account.json` |
| Testers still on old build | Draft never published, or Play lag, or debug APK |
| Download error after Play tap | Debug and Play signatures fighting. Uninstall first |
| Opt-in 404 | Wrong Google account in the browser or Play app |
| Production apply refused | 0 closed testers. Internal does not count |

---

## Command list

From the repo root:

```
./store/scripts/push-release.sh "one line of what changed"
```

That is the whole push. Skill: `.cursor/skills/push-play-release/SKILL.md`. Slash command: `/push`.

Pieces:

```
./store/scripts/bump-version.sh
./store/scripts/build-aab.sh
./store/scripts/push-release.sh --build-only "notes"
python3 store/scripts/patch-docs.py --code N --name 0.N.0 --notes "..."
```

Manual fallback if ego-browser is down: bump, `./gradlew bundleRelease`, upload `app/build/outputs/bundle/release/app-release.aab` in Console, then `patch-docs.py`.

