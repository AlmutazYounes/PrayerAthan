# Handoff: Prayer engine

Status: done

When: 2026-08-27
Agent: prayer-engine

## What landed

- `app/src/main/java/com/mutazyounes/prayerathan/engine/PrefsLocationStore` in `LocationStore.kt`
- `SavedLocation.parse` for settings (lat/long range + `ZoneId.of`)
- `PrayerAthanApp.locationStore` is now `PrefsLocationStore(this)`, lazy so Context is attached
- Tests: `PrefsLocationStoreTest.kt`, `SavedLocationParseTest` in the same file

`InMemoryLocationStore` stays for unit tests and `PrayerCalculator`'s default. Production reads SharedPreferences `prayerathan_location`. Empty prefs, a junk timezone, or unreadable doubles return Albany. Write of Amman round-trips on a fresh store against the same backend. Math is unchanged: adhan2 `NORTH_AMERICA` + `SHAFI`.

## Tests

Engine tests: 18 passed (`PrayerCalculatorTest` 9, `PrefsLocationStoreTest` 5, `SavedLocationParseTest` 4).

`./gradlew test` itself is not exit 0. Two `WeatherClientTest` cases fail on `org.json.JSONObject` not mocked on the JVM. That is weather, not this store. Engine tests do not touch JSON.

Covered here:

- Albany default when prefs are empty
- Write Amman, new `PrefsLocationStore` on the same fake prefs reads Amman
- Invalid zone and unreadable coords fall back to Albany
- `PrayerCalculator` sees the write on the next `location()` / `day()`
- `SavedLocation.parse` rejects unknown zones and out-of-range coords
- Existing `PrayerCalculatorTest` still uses `InMemoryLocationStore`

## What the next agent should know

Designer / settings: call `SavedLocation.parse` then `locationStore.write`. Pass `PrayerAthanApp.locationStore`. Do not geocode. Do not import adhan2.

GPS-once (shell later): write the same `SavedLocation` through this store. Timezone is an IANA id, not GPS.

Audio: still schedule from `PrayerDay`. Location change will change the next `day()` instants. Reschedule after a write if you already cache times.

## Contract drift

Did `ops/contracts/engine-api.md` change? yes. Persistence now names `PrefsLocationStore`, prefs keys, and `SavedLocation.parse`.

## Blockers

None.
