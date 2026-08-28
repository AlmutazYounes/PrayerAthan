package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerInstant
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant

internal fun remainingAthanAlarms(
    day: PrayerDay,
    now: Instant,
    mutedPrayers: Set<PrayerName> = emptySet(),
): List<PrayerInstant> {
    val targets = PrayerName.athanTargets().toSet() - mutedPrayers
    val remainingToday = day.times.filter { instant ->
        instant.name in targets && instant.at.isAfter(now)
    }
    if (day.nextAthan !in targets || !day.nextAthanAt.isAfter(now)) {
        return remainingToday
    }
    val next = PrayerInstant(day.nextAthan, day.nextAthanAt)
    val covered = remainingToday.any { it.name == next.name && it.at == next.at }
    return if (covered) remainingToday else remainingToday + next
}
