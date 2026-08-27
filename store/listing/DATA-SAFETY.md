# Data safety answers

Play Console → Policy → App content → Data safety.

These answers assume the APK that ships still matches today: nothing leaves the device, no analytics SDK, no crash uploader, no ads, no accounts, no Play Billing Library in the app. Location is on-device only. GPS is unused. `MainActivity` never requests location. Albany coordinates are baked in.

If any of that changes, redo this form and rewrite `store/PRIVACY.md` the same week. Reviewers compare the form, the policy, the merged manifest, and the AAB.

Closed, open, and production tracks need this form. Internal testing does not. The privacy URL is live. See below.

Official: https://support.google.com/googleplay/android-developer/answer/10787469

Google's word "collect" means data is transmitted off the device. On-device prayer math and a saved lat/long on disk are not collection.

## Privacy policy URL

```
https://almutazyounes.github.io/prayerathan-privacy/
```

Hosted from `store/privacy/index.html` on GitHub Pages. Public, no login, labeled privacy policy. App name, package, publisher Mutaz Younes, and developer name YounesM appear on that page.

## Overview

Does your app collect or share any of the required user data types?

No.

Answer No only while the app and every SDK in it send nothing off the device. adhan-kotlin computes times locally. There is no Firebase, AdMob, Crashlytics, or Sentry. Check the merged manifest for `INTERNET` before each AAB. If a library starts uploading, this becomes Yes and you declare those types.

## Data types

Because the overview answer is No, Console should skip the per-type checklist. If it still shows the list, leave every type unchecked. None collected. None shared.

| Category | Types | Collected | Shared |
| --- | --- | --- | --- |
| Location | Approximate location, Precise location | No | No |
| Personal info | Name, Email address, User IDs, Address, Phone number, Race and ethnicity, Political or religious beliefs, Sexual orientation, Other info | No | No |
| Financial info | User payment info, Purchase history, Credit score, Other financial info | No | No |
| Health and fitness | Health info, Fitness info | No | No |
| Messages | Emails, SMS or MMS, Other in-app messages | No | No |
| Photos and videos | Photos, Videos | No | No |
| Audio files | Voice or sound recordings, Music files, Other audio files | No | No |
| Files and docs | Files and docs | No | No |
| Calendar | Calendar events | No | No |
| Contacts | Contacts | No | No |
| App activity | App interactions, In-app search history, Installed apps, Other user-generated content, Other actions | No | No |
| Web browsing | Web browsing history | No | No |
| App info and performance | Crash logs, Diagnostics, Other app performance data | No | No |
| Device or other IDs | Device or other IDs | No | No |

### Location, said honestly

Do not declare Approximate location or Precise location as collected.

Saved city label, latitude, longitude, and timezone stay on the tablet for ISNA math. They are not sent to a server or to other users.

Today GPS is unused. The manifest may still list `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`. The app does not request them at runtime. QA with GPS denied keeps the Albany header. Drop unused location permissions before a store AAB, or implement the one-shot GPS in `PROJECT.md`. Either way, on-device processing is still not "collected" on this form.

When GPS ships, still do not declare collection unless coordinates leave the device. The privacy policy already mentions location because Play treats device location as sensitive even when it never leaves the phone.

Do not add `ACCESS_BACKGROUND_LOCATION`.

### Purchase history

This listing is free. There is no Play Billing Library and no IAP. Do not declare Purchase history. If IAP is added later, declare it.

### Device or other IDs

Do not declare this. No Advertising ID. No analytics reading `ANDROID_ID`. Recheck if any SDK lands in the release binary.

## Security practices

Is all of the user data collected by your app encrypted in transit?

N/A. Nothing is collected, so nothing is in transit. Do not check Yes as if there were TLS to your server. If Console hides the question after a No on overview, leave it hidden.

Do you provide a way for users to request that their data is deleted?

The app does not collect user data and does not offer accounts. Uninstall, or clear app storage in Android settings, wipes on-device location. Do not invent a deletion URL. Do not claim an in-app delete-account flow.

If Console still forces a yes/no on deletion for a no-collection app, pick the option that says users can delete data by uninstalling or clearing app data, not a web form.

Independent security review (MASA / lab badge)

No. Skip. That is a paid lab review, not a store requirement.

Committed to follow the Play Families policy badge

No. This app is not for children. Target audience is 13+. See `IARC.md`.

Unified Payments Interface badge

No. Skip.

## Account deletion, separate App content row

Does your app allow users to create an account?

No, my app does not allow users to create an account.

Leave reviewer credentials empty. There is no login.

## Ads declaration, also App content

Does your app contain ads?

No.

## What users should see on Play

The Data safety card should say the app does not collect or share user data.

If Play shows location, analytics, or ads on that card, the form is wrong or a library started talking. Fix the AAB and the form before anyone outside internal testing installs.
