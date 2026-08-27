# Engine API

Prayer engine owns this. Designer and audio consume it. Nobody else computes prayer times.

Package: `com.mutazyounes.prayerathan.engine`

Library: `com.batoulapps.adhan:adhan2`. `CalculationMethod.NORTH_AMERICA`. `Madhab.SHAFI`.

If implementation names drift, patch this file in the same change as the code.

## Types

```kotlin
enum class PrayerName {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}

data class SavedLocation(
    val label: String,          // "Albany, NY"
    val latitude: Double,       // default 42.6526
    val longitude: Double,      // default -73.7562
    val timeZoneId: String,     // default "America/New_York"
)

data class PrayerInstant(
    val name: PrayerName,
    val at: Instant,            // absolute
)

data class PrayerDay(
    val localDate: LocalDate,   // in SavedLocation.timeZoneId
    val times: List<PrayerInstant>, // six, Fajr through Isha
    val nextAthan: PrayerName,  // never SUNRISE
    val nextAthanAt: Instant,   // may be tomorrow Fajr
)

data class WallClocks(
    val albany: ZonedDateTime,  // SavedLocation zone
    val jordan: ZonedDateTime,  // always ZoneId.of("Asia/Amman")
)
```

`PrayerName.athanTargets()` is Fajr, Dhuhr, Asr, Maghrib, Isha.

Sunrise is in `times`. It is never `nextAthan`. No athan at sunrise. After Fajr, next is Dhuhr even if sunrise is still ahead.

## Functions

```kotlin
interface PrayerEngine {
    fun location(): SavedLocation
    fun day(now: Instant, location: SavedLocation = location()): PrayerDay
    fun clocks(now: Instant, location: SavedLocation = location()): WallClocks
    fun remainingToNext(now: Instant, location: SavedLocation = location()): Duration
}
```

`remainingToNext` is `nextAthanAt - now`, floored at zero. Tick from the UI every second by passing a new `now`. Do not hide a clock inside the engine.

Jordan is `now.atZone(ZoneId.of("Asia/Amman"))`. Never `albany plus hours`.

## Persistence

`LocationStore` reads and writes `SavedLocation`. First launch returns Albany defaults. Production is `PrefsLocationStore(context)` (SharedPreferences file `prayerathan_location`, keys `label`, `latitude`, `longitude`, `timeZoneId`). Engine tests may use `InMemoryLocationStore`. `PrefsLocationStore` tests inject `LocationPrefsBackend`.

Settings calls `SavedLocation.parse(label, latitude, longitude, timeZoneId)` before write. It returns null for unknown timezone ids (`ZoneId.of` throws) or coords outside lat [-90, 90] / long [-180, 180]. City comes from the bundled GeoNames list. Do not geocode over HTTP.

`PrefsLocationStore.read` falls back to Albany on empty prefs, unreadable values, or an invalid stored zone. Do not crash. After `write`, the next `location()` / `day()` call uses the new coords.

## What UI may do

Call `day`, `clocks`, `remainingToNext` from a ViewModel. Format 12-hour in the UI layer using `DESIGN.md` rules. Do not import adhan-kotlin from `ui/`.

## What audio may do

Read `PrayerDay.nextAthanAt` and the five athan instants for the local date plus tomorrow Fajr if needed. Schedule those. Do not recompute with a second library.
