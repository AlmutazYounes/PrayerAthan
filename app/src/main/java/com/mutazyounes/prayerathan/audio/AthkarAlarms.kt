package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

internal fun remainingAthkarAlarms(
    day: PrayerDay,
    now: Instant,
    zone: ZoneId,
): List<Instant> {
    val fajr = day.times.firstOrNull { it.name == PrayerName.FAJR }?.at ?: return emptyList()
    val isha = day.times.firstOrNull { it.name == PrayerName.ISHA }?.at ?: return emptyList()
    val athanTimes = day.times
        .filter { it.name in PrayerName.athanTargets() }
        .map { it.at }
    val today = hoursInWindow(fajr, isha, now, zone, athanTimes)
    if (today.isNotEmpty()) return today
    if (now.isBefore(isha)) return emptyList()
    if (day.nextAthan != PrayerName.FAJR) return emptyList()
    if (!day.nextAthanAt.isAfter(now)) return emptyList()
    val next = firstAllowedHourAfter(day.nextAthanAt, zone)
    return if (next.isAfter(now)) listOf(next) else emptyList()
}

internal fun isAthkarWindow(day: PrayerDay, now: Instant, zone: ZoneId): Boolean {
    val fajr = day.times.firstOrNull { it.name == PrayerName.FAJR }?.at ?: return false
    val isha = day.times.firstOrNull { it.name == PrayerName.ISHA }?.at ?: return false
    if (!now.isAfter(fajr) || !now.isBefore(isha)) return false
    return isAthkarAllowedHour(now.atZone(zone).hour)
}

internal fun isAthkarAllowedHour(hour: Int): Boolean = hour in 8..21

internal fun isAthanMinute(athanTimes: List<Instant>, at: Instant, zone: ZoneId): Boolean {
    val local = at.atZone(zone)
    return athanTimes.any { other ->
        val otherLocal = other.atZone(zone)
        otherLocal.toLocalDate() == local.toLocalDate() &&
            otherLocal.hour == local.hour &&
            otherLocal.minute == local.minute
    }
}

internal fun athanInstants(day: PrayerDay): List<Instant> {
    return day.times.filter { it.name in PrayerName.athanTargets() }.map { it.at }
}

private fun hoursInWindow(
    fajr: Instant,
    isha: Instant,
    now: Instant,
    zone: ZoneId,
    athanTimes: List<Instant>,
): List<Instant> {
    var cursor = firstHourAfter(fajr, zone).atZone(zone)
    val end = isha.atZone(zone)
    val hours = mutableListOf<Instant>()
    var guard = 0
    while (cursor.isBefore(end) && guard < 24) {
        val instant = cursor.toInstant()
        if (instant.isAfter(now) &&
            isAthkarAllowedHour(cursor.hour) &&
            !isAthanMinute(athanTimes, instant, zone)
        ) {
            hours += instant
        }
        cursor = cursor.plusHours(1)
        guard++
    }
    return hours
}

private fun firstHourAfter(instant: Instant, zone: ZoneId): Instant {
    return instant.atZone(zone).truncatedTo(ChronoUnit.HOURS).plusHours(1).toInstant()
}

private fun firstAllowedHourAfter(instant: Instant, zone: ZoneId): Instant {
    var cursor = firstHourAfter(instant, zone).atZone(zone)
    var guard = 0
    while (!isAthkarAllowedHour(cursor.hour) && guard < 24) {
        cursor = cursor.plusHours(1)
        guard++
    }
    return cursor.toInstant()
}
