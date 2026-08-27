# Play publisher service account

`./gradlew publishReleaseBundle` uploads straight to the Play Console internal
track (`4701680380313434468`, see `store/README.md`) using
[gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher).
It authenticates with a service account key that only Mutaz can create,
because it is tied to his Google Play Console account.

## One-time setup (Mutaz only)

1. Google Cloud Console, same project as the Play Developer API, or create a
   new project.
2. APIs & Services -> Enable "Google Play Android Developer API".
3. IAM & Admin -> Service Accounts -> Create service account. Any name, e.g.
   `prayerathan-publisher`. Skip the optional grant-access-to-project step.
4. Open the new service account -> Keys -> Add key -> Create new key -> JSON.
   Downloads a `.json` file.
5. Play Console -> Setup -> API access -> link the Cloud project if not
   already linked -> find the new service account in the list -> Grant access.
   Give it "Release manager" permission on PrayerAthan (upload to internal/
   closed tracks). Admin also works but is broader than needed.
6. Save that JSON file as `play/service-account.json` in this repo. Gitignored,
   same pattern as `keystore/keystore.properties`. Back it up somewhere Mutaz
   controls, same as the upload keystore.

## After that

`./gradlew bundleRelease publishReleaseBundle` builds a signed AAB and
uploads it to the internal track as a draft release. Draft means it still
needs a manual "Review release" -> "Publish" click in Play Console, so this
cannot silently ship to testers.

Bump `versionCode` / `versionName` in `app/build.gradle.kts` before every
publish. Play rejects a re-upload at the same versionCode.

If `play/service-account.json` does not exist, the `play {}` block in
`app/build.gradle.kts` skips setting credentials and the publish task fails
with an auth error. `bundleRelease` alone still works with no credential.
