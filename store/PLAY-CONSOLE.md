# Play Console, the actual walk

Checked 27 August 2026. If a number below might have moved, the URL is the source.

**Live state is `store/README.md`.** Repeatable AAB upload is `store/PUSH.md`. This file is signup, leftover declarations, and policy forms. The app is free. Production is locked on 12/14.

This is a new app with package `com.mutazyounes.prayerathan`. New apps use Android App Bundles and Play App Signing. An APK upload will be rejected.

Console IDs: developer 5934139594166642747, app 4974554092638251166, internal track 4701680380313434468.

## Developer account

Sign up: https://support.google.com/googleplay/android-developer/answer/6112435

- You must be 18.
- One-time fee: US$25. Visa, Mastercard, Amex. Discover in the US. No prepaid cards. Invalid ID can eat the fee with no refund.
- Account type is Personal or Organization. Personal is Mutaz, his legal name, his address. Organization is a company with a D-U-N-S number, registration docs, and an authorized-representative ID. US doc lists: https://support.google.com/googleplay/android-developer/answer/15633622
- Identity is tied to a Google payments profile. Names and addresses must match. https://support.google.com/googleplay/android-developer/answer/10841920
- Contact email, developer email, and phone are OTP-verified and must stay alive. https://support.google.com/googleplay/android-developer/answer/13628312

Personal accounts created after 13 November 2023 also need:

1. Device verification in the Play Console Android app.
2. Closed testing with 12 testers opted in continuously for 14 days before production. Official rule: https://support.google.com/googleplay/android-developer/answer/14151465

Older personal accounts and Organization accounts skip that tester gate. Your Console is the source of truth for which bucket you are in.

Separate from Play: starting 30 September 2026, Android developer verification for sideloaded installs begins in Brazil, Indonesia, Singapore, and Thailand, then expands. That is the Android Developer Console, not Play Console. Hobby sideload to 20 devices has a limited path. Full distribution of APKs outside Play is a different $25-ish verification. https://support.google.com/android-developer-console/answer/16561738

For Mutaz's two wall tablets, sideload still works. Play production on this personal account still needs 12 closed testers for 14 days.

## Play App Signing, AAB vs APK

New apps must upload an `.aab`. Play App Signing is required for that format.

https://developer.android.com/studio/publish/app-signing
https://developer.android.com/guide/app-bundle/faq

Two keys:

- **Upload key.** You generate this. You sign every AAB with it. Lose it and you request a reset in Console. Annoying, not fatal.
- **App signing key.** Google holds this and signs the APKs that users install. This is the identity Android checks on update.

Let Google generate the app signing key unless you already have a key you must keep for another store. Keep the upload keystore off Git, with a backup Mutaz actually controls.

Upload keystore is `keystore/` (gitignored). Release `signingConfig` is wired. Internal testing already has `bundleRelease` 6 (0.6.0). Debug APK still cannot go to Play.

Debug-signed and Play-signed installs of the same package cannot update each other. Once the wall tablets are on Play, stop sideloading debug over them.

## Target API

https://support.google.com/googleplay/android-developer/answer/11926878

From 31 August 2026, new apps and updates for phones, tablets, and foldables must target Android 16, API 36, except Wear / Automotive / TV / XR which have lower floors.

This repo already has `targetSdk = 36` and `compileSdk = 36` in `app/build.gradle.kts`. You are on the right side of that deadline as of 27 August 2026. Next year's floor will move again. Budget a target-SDK bump each summer.

Existing apps that are already on the store and not updating have a lower stay-available floor. Irrelevant until you have a live listing.

## 16 KB page size

https://developer.android.com/guide/practices/page-sizes

Apps targeting API 35+ must support 16 KB memory pages on 64-bit devices. Google's page, as of early August 2026, says noncompliant updates are blocked from 1 February 2027. Older posts still say 1 November 2025 or 31 May 2026. Those dates moved.

Pure Java / Kotlin with no native `.so` already complies. PrayerAthan is Kotlin, Compose, and `com.batoulapps.adhan:adhan2`. There is no NDK in this project. Still open the first release AAB in APK Analyzer and look for `lib/arm64-v8a`. If that folder is empty, you are done. If a dependency sneaks in a `.so`, that file must have ELF LOAD alignment of 16 KB.

AGP in this repo is 9.3.2, which is new enough that your own native code would be aligned. Third-party prebuilts are the usual failure.

## Closed testing, then production

Tracks:

| Track | Who sees it | When you can use it |
| --- | --- | --- |
| Internal | Up to 100 email addresses you pick | Before listing is complete. Fast. |
| Closed | People who opt in | After app setup. Required for new personal accounts. |
| Open | Anyone on Play who joins the test | After production access. |
| Production | The store | After the 12 / 14 gate and a questionnaire, for new personal accounts. |

Closed-test rule for those personal accounts: at least 12 testers opted in continuously for the last 14 days when you apply. Opt-in means they opened the Play opt-in link and joined. A spreadsheet of emails is not 12 testers. If someone leaves on day 11, that person does not count. Recruit 14 to 16.

Testers must have Google accounts. They install from Play, not from a Drive APK.

This listing is already **free**. Closed testers install without buying. Internal testers still do not count toward 12/14. https://support.google.com/googleplay/android-developer/answer/9845334

Dashboard → Apply for production after 12 testers have been opted in for 14 days. Tried 27 Aug 2026 with 0 closed testers. Google: application could not be submitted. Review after a valid apply is often 7 days or less.

## Store listing fields

Official setup: https://support.google.com/googleplay/android-developer/answer/9859152
Graphics: https://support.google.com/googleplay/android-developer/answer/9866151
Metadata policy: https://support.google.com/googleplay/android-developer/answer/9898842

| Field | Limit / spec | Note for this app |
| --- | --- | --- |
| App name | 30 characters | `Athan Wall Clock` is 16. Fine. |
| Short description | 80 characters | First text on the listing. |
| Full description | 4000 characters | Say wall tablet. Say offline. Do not promise Quran. |
| Hi-res icon | 512 x 512, 32-bit PNG with alpha, ≤ 1024 KB | Separate from the adaptive launcher. |
| Feature graphic | 1024 x 500, JPEG or 24-bit PNG, no alpha | Required to publish. |
| Screenshots | JPEG or 24-bit PNG, no alpha. 320 to 3840 px. Long side at most 2x short side. Min 2 across device types. Max 8 per type. | Phone, 7-inch tablet, 10-inch tablet are separate slots. |
| Privacy policy URL | Public https page | Required. See `POLICY-AND-LEGAL.md`. |
| Category | One | Lifestyle. |
| Contact email | Working | Same one you verified. |
| Video | Optional YouTube | Skip until the wall looks like the mockups on hardware. |

Do not put price, "official", "#1", or "download now" in the title, icon, or short description.

### Tablet screenshots

Canonical files and sizes: `store/listing/README.md`. Upload from `store/listing/play/` (16:9 / 9:16, 1080px+). Do not upload settings. Do not upload `design/athan-wall-*.png`.

Phone slot is the 7-inch wall in 9:16. Console still asks for it.

### Listing copy

Paste from `store/listing/COPY.md`. That text is already in Console.

## Content rating

https://support.google.com/googleplay/android-developer/answer/9898843

Every app fills the IARC questionnaire under Policy → App content. Unrated apps get removed. July 2026 policy note: https://support.google.com/googleplay/android-developer/answer/17134731

Category is a utility / lifestyle clock. No user-generated content, no chat, no gambling. If GPS ships, the questionnaire asks about location sharing. You share location with nobody. It stays on device.

Expect something in the Everyone / PEGI 3 neighborhood. Do not target children.

## Data safety

https://support.google.com/googleplay/android-developer/answer/10787469

Required for closed, open, and production. Already saved on this app. A privacy policy URL is required to complete the form.

Google's word "collect" means data leaves the device. On-device processing that never goes to a server is not "collected" on this form. Prayer math and a saved lat/long on disk can be declared as no data collected and no data shared, if that stays true.

That is only honest if:

- No analytics SDK
- No crash reporter that uploads stacks
- No AdMob
- No Play Billing Library. This listing is free. There is no in-app purchase.
- Location is not sent anywhere

If you add Firebase, ads, or IAP, redo the form. Purchase history becomes a collected type for IAP.

Encryption in transit: N/A if nothing leaves. Independent security review: skip, that is paid lab theater.

Account deletion: you have no accounts. Answer that the app does not offer account creation. Do not invent a deletion URL.

Match the privacy policy to the form. Reviewers compare them.

## Declarations this app will hit

Play scans the merged manifest. This one currently asks for exact alarms, a mediaPlayback FGS, boot, notifications, and fine location. Expect a form for each sensitive one.

### Exact alarms: `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM`

https://support.google.com/googleplay/android-developer/answer/16558241
https://developer.android.com/about/versions/14/changes/schedule-exact-alarms

`USE_EXACT_ALARM` is restricted. It is for apps whose core, user-facing job is an alarm, timer, or calendar with event notifications. PrayerAthan's job is to fire an athan at a precise instant. That is an alarm clock. Say that in the declaration. Show a video of the wall sitting idle, the time hitting a prayer, the MP3 starting, the NOW block appearing.

Do not claim calendar sync or a generic "reminder." Do not use exact alarms for analytics.

`SCHEDULE_EXACT_ALARM` is the user-grantable version. On Android 14+ it is denied by default for new installs. A wall tablet that needs Mutaz to hunt through Settings after every ROM update will miss Fajr. Prefer `USE_EXACT_ALARM` if Play accepts the alarm-clock case, and drop the other permission so the merged manifest is not noisy.

This repo currently declares both. That is a review question waiting to happen. See `APP-GAPS.md`.

### Foreground service, type `mediaPlayback`

https://developer.android.com/develop/background-work/services/fgs/service-types
https://support.google.com/googleplay/android-developer/answer/13392821

`AthanService` is `foregroundServiceType="mediaPlayback"` with `FOREGROUND_SERVICE_MEDIA_PLAYBACK`. It runs only while the file plays, shows a notification with Stop, and dies on tap or completion.

Play Console → App content → declare `mediaPlayback`. You will need:

- What it does: play the athan MP3 when a scheduled prayer starts, so Android does not kill the process.
- What the user notices if it is interrupted: the call to prayer stops mid-file.
- A video: alarm fires or you start playback, notification appears, audio is audible, tap Stop, service ends. Host on YouTube or a public Drive link.

Use case is audio playback, user-perceptible. It is started by an alarm the user installed the app to get, not by a hidden job.

Android 15+ refuses to start a `mediaPlayback` FGS from `BOOT_COMPLETED`. This app already does the right thing. `BootReceiver` only reschedules `AlarmManager`. It does not start `AthanService`. Do not "fix" boot by launching the player.

### Location, `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`

https://support.google.com/googleplay/android-developer/answer/10144311
https://support.google.com/googleplay/android-developer/answer/17033915

Today the manifest has both FINE and COARSE. `MainActivity` never requests them. QA on 27 August 2026: GPS denied, header stays Albany, no prompt. That is a policy smell. Either implement the one-shot GPS `PROJECT.md` already specified, or delete the unused permissions before upload.

Prayer times need city-level coordinates. COARSE is enough for ISNA math. FINE is nicer for a house at a city edge and harder to justify under minimum-scope rules.

From April 2026 Play has been moving toward a "location button" for one-shot precise location on Android 17+. Declaration for FINE is expected in Console later in 2026, with enforcement talk around late 2026 into January 2027. You target 36, not 37, so the button is not mandatory on the binary you have. Do not declare FINE "just in case."

You do not need `ACCESS_BACKGROUND_LOCATION`. Do not add it. GPS is one fix while the user is in settings, then save.

If location stays on device, Data safety can still say not collected. The User Data policy still wants a privacy policy that mentions location access, because device location is sensitive even when it never leaves the phone.

### Notifications and boot

`POST_NOTIFICATIONS` is requested on launch so the FGS notification can show on API 33+. Honest. The athan notification is the Stop affordance, not spam.

`RECEIVE_BOOT_COMPLETED` plus `LOCKED_BOOT_COMPLETED` is how alarms return after reboot. Declare it as alarm reschedule, not as starting a service at boot.

## Sources, checked 27 August 2026

- Register: https://support.google.com/googleplay/android-developer/answer/6112435
- Testing gate: https://support.google.com/googleplay/android-developer/answer/14151465
- Target API: https://support.google.com/googleplay/android-developer/answer/11926878
- 16 KB: https://developer.android.com/guide/practices/page-sizes
- Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- User data / privacy policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Exact alarms: https://support.google.com/googleplay/android-developer/answer/16558241
- FGS declaration: https://support.google.com/googleplay/android-developer/answer/13392821
- Listing graphics: https://support.google.com/googleplay/android-developer/answer/9866151
- Prices, free vs paid: https://support.google.com/googleplay/android-developer/answer/6334373
- Service fees: https://support.google.com/googleplay/android-developer/answer/112622
