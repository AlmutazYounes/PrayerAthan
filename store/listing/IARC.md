# Content rating and audience

Two Console jobs. Do not mix them up.

Target audience is 13+. That keeps this wall clock off the Families policy. IARC then rates the content. A utility clock with no chat, no UGC, and no gambling should land near Everyone / PEGI 3. Do not inflate violence or "users interact" to force a teen IARC rating. 13+ is the audience checkbox, not the content rating.

Official IARC help: https://support.google.com/googleplay/android-developer/answer/9898843
Official ratings walk: https://support.google.com/googleplay/android-developer/answer/9859655

Console wording moves. If a prompt is missing, skip it. If a new prompt appears, answer it the same way: this is a tablet clock, not a social app, not a game, not for children.

Email for IARC correspondence: mutazyounes@gmail.com

## Target audience and content

Play Console → Policy → App content → Target audience and content.

Designed for children?

No.

Appeals to children?

No. The person hanging a keep-screen-on tablet is the audience.

Target age groups

- Ages 5 and under: no
- Ages 6 to 8: no
- Ages 9 to 12: no
- Ages 13 to 15: yes
- Ages 16 to 17: yes
- Ages 18 and over: yes

Do not tick any group under 13. One under-13 tick pulls in Families policy, ads SDK rules, and review you do not want for a kiosk clock.

News app?

No.

COVID-19 contact tracing and status apps?

No.

## IARC questionnaire

Play Console → Policy → App content → Content ratings → Start questionnaire.

Category

All other apps, or Utility if that is the label. Not Game. Not Social or communication. Lifestyle is the store category. IARC still wants the non-game bucket.

### Content

Does the app contain violence, including cartoon or fantasy violence?

No.

Blood or gore?

No.

Weapons used against people or animals?

No.

Sexual content, nudity, or dating?

No.

Profanity, crude language, or crude humor?

No.

Drug, alcohol, or tobacco use?

No.

Gambling, betting, or simulated gambling, including loot boxes or prize wheels?

No.

Horror, jump scares, or content meant to frighten?

No.

Discrimination, criminal activity, or graphic injury, if asked?

No.

### Interactive elements

Can users interact or communicate with each other, including chat, comments, or social networking?

No.

User-generated content that other people can see?

No.

Can users share their real-time or historical location with other users?

No. Location for prayer math stays on the tablet. It is not shared. Today GPS is unused.

Can users share personal information with other users?

No.

Unrestricted internet access, an unfiltered browser, or in-app web that can reach arbitrary sites?

No. The app manifest has no `INTERNET` permission. Times are computed on device.

Digital goods for sale inside the app, including IAP, subscriptions, or random-item purchases?

No. This listing is free. There is no in-app catalog.

Does the app contain ads?

No.

### Location on this form

IARC "shares location" means sharing with other people. It does not mean Albany coordinates sitting in local storage. Answer No.

If a later version uses one-shot GPS for a city fix, retake the questionnaire only if users can send that location to someone else. On-device GPS still is not sharing.

## What to expect after Calculate rating

Regional labels should look like Everyone, PEGI 3, IARC 3+, or the local equivalent. Interactive-element descriptors should be empty. If Play prints Users Interact, Shares Location, Unrestricted Internet, or In-app Purchases, a Yes was wrong. Edit and recalculate.

Apply the rating. Keep the certificate email.

Retake the questionnaire if you add chat, UGC, ads, IAP, or location sharing. Unrated apps get removed.
