# Policy and legal

This is not a lawyer. It is the list of things that will get a listing rejected or, worse, a copyright complaint after it is live.

Do not paste AGPL or MAWAQIT code into this app. Do not "borrow" Al-Azan. Our code is MIT. Their licenses are not a buffet.

## Privacy policy, even with no backend

Play's User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311

Every app must have a privacy policy. That includes apps that never talk to a server. This one has:

1. A public https URL in Play Console. Live page https://almutazyounes.github.io/prayerathan-privacy/ Hosted from `store/privacy/index.html`. Not a PDF. Not behind a login.
2. The same policy inside the app. A link in the long-press settings sheet is enough. Today there is no link.

You do not need an account-deletion web form. This app does not create accounts. If someone later adds logins, deletion in-app and on the web becomes mandatory. Do not add logins.

Hosted on GitHub Pages. A Notion link that breaks is how reviews stall.

### What the page should say, in plain language

- Who publishes the app. Mutaz Younes, or whatever name is on the Play listing.
- Package `com.mutazyounes.prayerathan`.
- Prayer times are calculated on the device. No timetable is fetched from the internet.
- Location: if GPS or a typed city is saved, it stays on the device for prayer math. It is not uploaded. Default is Albany, NY coordinates baked into the app.
- Alarms, notifications, and the boot receiver exist so athan still fires after reboot.
- No accounts. No analytics, if that stays true. No ads, if that stays true.
- How to wipe it: uninstall, or clear app data.

If you add Crashlytics or AdMob later, rewrite the page the same week. Data safety must match.

Location is "personal and sensitive user data" on Play even when it never leaves the tablet. Mention it. If GPS is a user tap in settings, say that. Runtime permission, then one fix, then save.

## Permissions in the current manifest

From `app/src/main/AndroidManifest.xml` as of 27 August 2026:

| Permission | Why it is there | Play angle |
| --- | --- | --- |
| `ACCESS_COARSE_LOCATION` | Spec wants optional GPS / city. Not implemented. | Declare only if you use it. Coarse is enough for prayer math. |
| `ACCESS_FINE_LOCATION` | Same. Never requested at runtime. | Extra heat for little gain. Drop until a one-shot GPS exists. |
| `POST_NOTIFICATIONS` | FGS notification on API 33+. Requested in `MainActivity`. | Honest. |
| `RECEIVE_BOOT_COMPLETED` | `BootReceiver` reschedules alarms. | Honest. Direct boot too, `LOCKED_BOOT_COMPLETED`. |
| `SCHEDULE_EXACT_ALARM` | Exact athan times. | Restricted-ish. User can deny on 14+. |
| `USE_EXACT_ALARM` | Same job, install-granted for alarm clocks. | Restricted. Core-function test. See Console doc. |
| `WAKE_LOCK` | Alarm / player. | Normal. |
| `FOREGROUND_SERVICE` | `AthanService`. | Required for FGS. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Type on that service. | Needs a Console declaration and a demo video. |

There is no `INTERNET` permission in the app manifest. Keep it that way unless a library merges one in. Check the merged manifest before each AAB.

There is no `ACCESS_BACKGROUND_LOCATION`. Keep it that way. GPS is a foreground, user-started fix.

`android:allowBackup="true"` means a future persisted location could land in device backup. Fine. Say "stored on device" in the policy.

## Location

`PROJECT.md` allows three sources: Albany defaults, one GPS fix, manual city / lat-long plus timezone. Production code uses `InMemoryLocationStore` and never asks for GPS. Settings prints `ALBANY, NY` as a label.

For Play:

- Do not upload FINE if you do not call the location APIs.
- When GPS ships, ask at the moment of the tap, after a one-line explanation. "Used once to set prayer coordinates. Not shared."
- Coarse vs fine: city-level is enough. If FINE is in the APK, expect a minimum-scope declaration as Play rolls that out through late 2026. https://support.google.com/googleplay/android-developer/answer/17033915

## Notifications

The athan notification is ongoing only while the file plays. It has a Stop action. That is the model Play wants for `mediaPlayback`. Do not add marketing notifications. Do not add a daily "rate us."

## Exact alarms

The product is an alarm clock for five daily prayers. `AlarmManager.setAlarmClock` is the right API. `USE_EXACT_ALARM` matches "core functionality is an alarm." Write the declaration that way. Record a video of a prayer firing.

Shipping both `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM` looks like you were unsure. Pick one. Alarm-clock apps that Play accepts usually take `USE_EXACT_ALARM` so a kiosk tablet does not lose Fajr when the user never opened special app settings.

## Boot receiver

`BootReceiver` is exported, `directBootAware`, and listens for `BOOT_COMPLETED` and `LOCKED_BOOT_COMPLETED`. On receive it only calls `schedule`. It does not start `AthanService`. That is what Android 15 wants. Athan then fires from the restored alarm, which starts the service.

Do not add a foreground service start in the boot path to "make it more reliable."

## Audio copyright, the two MP3s

Read `audio/SOURCE.md`. Short version: personal use on Mutaz's wall. Replace before strangers install from Play.

**`fajr.mp3`.** Extracted from a 13 November 2009 Masjid al-Haram Fajr on Archive.org, item `MakkahAzan`, uploader Maahir. The item is tagged public domain. That tag is the uploader's claim. A live Haram recording from 2009 is likely still someone's copyright. Fine at home. Not a clearance to sell on Play.

**`standard.mp3`.** Isha from Masjid al-Haram by Ali ibn Ahmad Mala, 18 Muharram 1439, Archive.org item `0314zzzz181439`. Used for Dhuhr, Asr, Maghrib, and Isha. Whisper check: no الصلاة خير من النوم. The MP4 is YouTube-origin hosted on Archive.org. Personal use only. The old Kiwifu GitHub HQ file had the Fajr line and was replaced.

MIT on our Kotlin does not license the recordings. Shipping them on Play as if we own them is how you get a takedown, and it is a bad look for a mosque clock.

Replace with one of:

- A recording you commissioned, with a written license to distribute in an Android app.
- A file with a real public license from the rights holder, not from a zip blog.
- Your own recitation, if you are willing to hear it five times a day.

Keep Fajr distinct. Fajr should include الصلاة خير من النوم. Confirm by ear.

Do not rip YouTube Haramain videos and hope. Do not copy MAWAQIT's media.

## Trademarks, names, Makkah, Haramain

"Athan" and "Adhan" are ordinary words for the call to prayer. Other apps already use them. `PrayerAthan` as one word is more distinctive than "Athan" alone. Search Play before you fall in love with a shorter name. If you get a lawyer later, search USPTO / UKIPO too.

Do not imply official status. No "the official Makkah athan." No Haramain logos. No photograph of the Kaaba you do not have rights to. No Saudi ministry marks.

"Makkah" in the listing is a description of a voice, and only after the file is licensed. Until then, say "athan" and stop.

The gold Rub el Hizb launcher is a geometric Islamic star, not a government seal. Fine. Do not drop a Masjid al-Haram photograph into the feature graphic.

## MIT on our code vs the store listing

`PROJECT.md` says our code is MIT unless Mutaz says otherwise. There is no `LICENSE` file in the repo root yet. Add one before you tell the internet it is MIT, or the claim is oral.

MIT on the Kotlin means other people may copy the engine and the wall. This Play listing is free. Those are separate facts. The store is a distribution deal with Google, not a second copyright on the source.

Do not put "All rights reserved, do not copy" in the Play description if the GitHub repo is MIT. Pick one story.

adhan-kotlin is MIT. You already depend on it. Keep the attribution in an about line if you want to be decent. Not required by Play.

Do not fork MAWAQIT. CC BY-NC-SA is not MIT, and NC fights a Play listing. Do not fork Al-Azan. AGPL would infect a store binary in ways you do not want to test in public.

MosqueClock and athan-nightstand are MIT and were already used as layout references. That is fine.

## What Play will compare

Reviewers look at:

- The merged manifest vs the declarations vs the privacy policy vs Data safety vs what the app actually does.
- Unused FINE location.
- A settings toggle labeled "Adhan is playing" that does not play audio.
- Haramain branding without rights.
- A debug-looking `0.1.0` with a preview toggle, which reads as unfinished, which is a quality reject even when policy is clean.

Fix the product, then fill the forms so they match. Do not write a prettier policy than the APK.
