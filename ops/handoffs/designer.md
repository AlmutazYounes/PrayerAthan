# Handoff: Designer

Status: done

When: 2026-08-29
Agent: designer

## What was slow

Settings opened the 2.1MB / 34k-row `cities.tsv` on every sheet open, even after `CityCatalog.bundled` had already parsed it. `fold()` compiled a Unicode regex per city per keystroke. Opening a city menu with a blank query still searched that country's full pool. `match()` for the saved pin ran on the UI thread.

## What changed

- Wall warms the catalog on IO after the clock appears. Settings uses `CityCatalog.cached()` and skips the asset read when the parse is already in memory.
- Cities wait for two typed letters, then show at most 50 hits. Countries stay searchable and cap at 60 once the user types.
- Filter lists use `derivedStateOf`. Dropdowns stay `LazyColumn` with stable keys.
- `match()` after load runs on `Dispatchers.Default`.
- `fold()` uses one compiled regex. City name keys are stored at parse time so search does not renormalize every row.

Portrait and landscape sheet layout is unchanged. Same `settingsPanel` / gold / Cinzel fields. No lat/long typing.

## Files

- `app/src/main/java/com/mutazyounes/prayerathan/ui/SettingsSheet.kt`
- `app/src/main/java/com/mutazyounes/prayerathan/ui/WallScreen.kt` (prefetch only)
- `app/src/main/java/com/mutazyounes/prayerathan/engine/CityCatalog.kt`
- `app/src/test/java/com/mutazyounes/prayerathan/engine/CityCatalogTest.kt`

## Hangs

`./gradlew test assembleDebug` after this pass.
