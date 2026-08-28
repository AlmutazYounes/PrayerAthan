# Play plan

Mutaz 27 Aug 2026: listing is **free**. He asked to publish. Google blocked production until closed testing.

This package stays $0. Play will not let `com.mutazyounes.prayerathan` go free to paid. Official: https://support.google.com/googleplay/android-developer/answer/6334373 A later paid clock needs a new `applicationId`.

No IAP. No ads. No accounts. No second package.

Product behavior is still `PROJECT.md`.

## Done in Console

- App created. Then converted to free.
- Signed AAB on internal testing, version 8 / 0.8.0.
- Store listing filled: PrayerAthan, short/full copy, icon, feature graphic, phone + 7-inch + 10-inch shots.
- Privacy, sign-in none, ads no, IARC Everyone / PEGI 3, audience 13+, Data safety none collected, government / financial / health no.

## Not done

1. **Google production access.** Personal account. Need a closed-test track with 12 testers opted in for 14 days, then Apply again. Internal testers do not count. Application on 27 Aug was rejected: 0 closed testers.
2. **Listing review.** Publishing overview still holds the pretty listing. The Play URL stays a gray stub until Google reviews a submitted change. Send for review was locked (dashboard 12/13, leftover merchant row on a free app).
3. **Closed testing Alpha.** Track exists. No countries saved, no release, no tester list of 12.
4. **App content leftovers.** Advertising ID, foreground service demo video, exact alarm demo video.
5. **APK honesty.** See `APP-GAPS.md`. Location dies on reboot. MP3s are personal-use. Settings still has an athan UI preview. No privacy link in the sheet.

## Order from here

1. Mutaz sends 12+ Google account emails.
2. Closed testing Alpha: countries, testers, promote the existing AAB, send that release for review.
3. Testers join the closed-test opt-in and stay 14 days.
4. Apply for production. Google review often about a week.
5. Meanwhile: persist location, licensed audio, drop unused GPS perms or implement GPS-once, hide the preview toggle, privacy link in settings.

Sideload still works for the two Albany walls. Uninstall debug before installing from Play.
