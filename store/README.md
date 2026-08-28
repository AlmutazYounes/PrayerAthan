# Store docs

How PrayerAthan sits on Google Play. Not the product spec. `PROJECT.md` wins for what the wall does.

## Where we are, 27 August 2026

| Thing | State |
| --- | --- |
| Package | `com.mutazyounes.prayerathan` |
| Price | **Free**. Cannot become paid on this package. |
| Production | Inactive. Apply disabled. 0 closed testers opted in. |
| Internal testing | Live. Release 9 (0.9.0). |
| Closed testing | Alpha track. Countries targeted. Release 7 draft. Not rolled out. 0 opted in. |
| Listing in Console | Name, copy, icon, graphic, six wall screenshots saved. |
| Listing on Play URL | Gray stub until Google reviews a closed or production send. |
| App content | Privacy, ads no, IARC, 13+, Data safety none, gov/finance/health no. Advertising ID no. FGS mediaPlayback + demo video. Exact alarm = alarm clock. |
| Privacy URL | https://almutazyounes.github.io/prayerathan-privacy/ |

Production page: https://play.google.com/store/apps/details?id=com.mutazyounes.prayerathan

Internal opt-in: https://play.google.com/apps/internaltest/4701680380313434468

Testers already on Play: open Play Store, search PrayerAthan (or the unreviewed package name), tap Update. Or reopen the opt-in link. Play can sit on the old build for 5 to 60 minutes. If Update never appears, the tablet still has the debug APK. Uninstall that, then install from the opt-in link.

Public production needs 12 closed testers opted in for 14 days, then Apply for production. Official: https://support.google.com/googleplay/android-developer/answer/14151465

## What to read

1. This file.
2. `PUSH.md`, how to upload the next AAB. Scripts in `store/scripts/`. Slash `/push`.
3. `PLAN.md`, what is left before public.
4. `CHECKLIST.md`, boxes.
5. `APP-GAPS.md`, what the APK still lacks.
6. `POLICY-AND-LEGAL.md`, the two MP3s.
7. `PLAY-CONSOLE.md`, Console IDs and leftover declarations.
8. `listing/`, copy and 16:9 shots already uploaded.
9. `MONEY.md`, why ads and IAP stay out. This listing is $0.

## Console IDs

- Google: mutazyounes@gmail.com
- Closed testing emails collected in untracked `store/testers.csv` (details in `store/TESTERS.md`)
- Developer: YounesM, personal
- Account: `5934139594166642747`
- App ID: `4974554092638251166`
- Internal track: `4701680380313434468`

## Do not

- Make the app paid. Play will not allow it on this package after free.
- Paste AGPL or MAWAQIT code.
- Claim official Makkah or Haramain.
- Sideload debug over a Play install of the same package.
