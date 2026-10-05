# Status

Updated: 2026-10-05

## Now

Play listing is **free**. Internal testing is **41 (0.41.0)**. Production is **not public**. Console 5 Oct 2026: 12 closed testers opted in for **13 days** continuously. One day left on the 14-day gate, then Apply for production.

The Play URL shows a gray stub (`com.mutazyounes.prayerathan (unreviewed)`) because listing changes sit in Publishing overview and have not been reviewed. Pictures are in Console. They are not on the store page yet.

Location is searchable country then city. Athan sound is selectable with a PLAY demo. Per-prayer mute lets user toggle athans for Fajr, Dhuhr, Asr, Maghrib, and Isha. Each prayer has its own volume slider. Hourly athkar can be turned off. No athkar from 10 PM until 8 AM. Use GPS takes one fix. Night blackout (11 PM - 4 AM) with tap-to-wake.

## Next

Hourly athkar and Night blackout (11 PM - 4 AM tap-to-wake) are in. Palettes landed. Wall fill is the two dark backdrop WebPs (dark wall only). Location settings landed. Device QA on the real 7-inch and 10-inch. Licensed MP3s before an honest public ship. Closed testing 12/14 after that.

## Board

| Job | Status | Agent file | Handoff |
| --- | --- | --- | --- |
| Spec / design | done | | `DESIGN.md`, two PNGs |
| Athan files | done | | `audio/SOURCE.md` |
| Control plane | done | `AGENTS.md`, `ops/` | |
| Prayer engine | done | `.cursor/agents/prayer-engine.md` | `ops/handoffs/engine.md` |
| Location persist | done | `.cursor/agents/prayer-engine.md` | `PrefsLocationStore` |
| Android shell | done | `.cursor/agents/android-shell.md` | `ops/handoffs/shell.md` |
| Designer | done | `.cursor/agents/designer.md` | `ops/handoffs/designer.md` |
| Location settings | done | `.cursor/agents/designer.md` | country/city search |
| Athan audio | done | `.cursor/agents/athan-audio.md` | `ops/handoffs/audio.md` |
| Device QA | waiting | `.cursor/agents/device-qa.md` | `ops/handoffs/qa.md` |
| Play Store | in Console | | `store/`, `ops/handoffs/store.md` |

## Blockers

Google production access: 12 closed testers / 14 days. Cannot skip on this personal account.

## Notes

- Production URL (stub until Google reviews): https://play.google.com/store/apps/details?id=com.mutazyounes.prayerathan
- Internal test: https://play.google.com/apps/internaltest/4701680380313434468
- Testers: `ClosedTesters` only. `store/testers.csv` (19 invited). Internal testing does not count.
- Privacy: https://almutazyounes.github.io/prayerathan-privacy/
- Wall tablets install from Play only. Do not sideload a debug APK over a Play install.
- Test athan: `adb shell am start-foreground-service -n com.mutazyounes.prayerathan/.audio.AthanService -a com.mutazyounes.prayerathan.audio.PLAY --es prayer DHUHR`
- Test athkar: `adb shell am start-foreground-service -n com.mutazyounes.prayerathan/.audio.AthkarService -a com.mutazyounes.prayerathan.audio.ATHKAR_PLAY`
