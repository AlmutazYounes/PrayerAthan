package com.mutazyounes.prayerathan.engine

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculator(
    private val locationStore: LocationStore = InMemoryLocationStore(),
) : PrayerEngine {

    private var cacheOne: Pair<TimesKey, List<PrayerInstant>>? = null
    private var cacheTwo: Pair<TimesKey, List<PrayerInstant>>? = null

    override fun location(): SavedLocation = locationStore.read()

    override fun day(now: Instant, location: SavedLocation): PrayerDay {
        val zone = ZoneId.of(location.timeZoneId)
        val localDate = now.atZone(zone).toLocalDate()
        val times = timesFor(localDate, location)
        val next = nextAthan(now, localDate, location, times)
        return PrayerDay(
            localDate = localDate,
            times = times,
            nextAthan = next.name,
            nextAthanAt = next.at,
        )
    }

    override fun clocks(now: Instant, location: SavedLocation): WallClocks {
        return WallClocks(
            albany = now.atZone(ZoneId.of(location.timeZoneId)),
            jordan = now.atZone(TimeZones.asiaAmman),
        )
    }

    override fun remainingToNext(now: Instant, location: SavedLocation): Duration {
        val remaining = Duration.between(now, day(now, location).nextAthanAt)
        return if (remaining.isNegative) Duration.ZERO else remaining
    }

    private fun nextAthan(
        now: Instant,
        localDate: LocalDate,
        location: SavedLocation,
        today: List<PrayerInstant>,
    ): PrayerInstant {
        val upcoming = today.firstOrNull { instant ->
            instant.name in ATHAN_TARGETS && instant.at > now
        }
        if (upcoming != null) {
            return upcoming
        }
        return timesFor(localDate.plusDays(1), location).first { it.name == PrayerName.FAJR }
    }

    private fun timesFor(localDate: LocalDate, location: SavedLocation): List<PrayerInstant> {
        val key = TimesKey(
            localDate,
            location.latitude,
            location.longitude,
            location.timeZoneId,
        )
        cacheOne?.let { if (it.first == key) return it.second }
        cacheTwo?.let { if (it.first == key) return it.second }
        val prayerTimes = PrayerTimes(
            Coordinates(location.latitude, location.longitude),
            DateComponents(localDate.year, localDate.monthValue, localDate.dayOfMonth),
            PARAMETERS,
        )
        val computed = listOf(
            PrayerInstant(PrayerName.FAJR, javaInstant(prayerTimes.fajr.toEpochMilliseconds())),
            PrayerInstant(PrayerName.SUNRISE, javaInstant(prayerTimes.sunrise.toEpochMilliseconds())),
            PrayerInstant(PrayerName.DHUHR, javaInstant(prayerTimes.dhuhr.toEpochMilliseconds())),
            PrayerInstant(PrayerName.ASR, javaInstant(prayerTimes.asr.toEpochMilliseconds())),
            PrayerInstant(PrayerName.MAGHRIB, javaInstant(prayerTimes.maghrib.toEpochMilliseconds())),
            PrayerInstant(PrayerName.ISHA, javaInstant(prayerTimes.isha.toEpochMilliseconds())),
        )
        cacheTwo = cacheOne
        cacheOne = key to computed
        return computed
    }

    private fun javaInstant(epochMilli: Long): Instant = Instant.ofEpochMilli(epochMilli)

    companion object {
        private val PARAMETERS = CalculationMethod.NORTH_AMERICA.parameters.copy(
            madhab = Madhab.SHAFI,
        )
        private val ATHAN_TARGETS = PrayerName.athanTargets().toSet()
    }
}

private data class TimesKey(
    val date: LocalDate,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
)
