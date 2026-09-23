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
    medicineSlots: List<MedicineSlot> = emptyList(),
): List<Instant> {
    val fajr = day.times.firstOrNull { it.name == PrayerName.FAJR }?.at ?: return emptyList()
    val isha = day.times.firstOrNull { it.name == PrayerName.ISHA }?.at ?: return emptyList()
    val athanTimes = day.times
        .filter { it.name in PrayerName.athanTargets() }
        .map { it.at }
    val today = hoursInWindow(fajr, isha, now, zone, athanTimes, medicineSlots)
    if (today.isNotEmpty()) return today
    if (now.isBefore(isha)) return emptyList()
    if (day.nextAthan != PrayerName.FAJR) return emptyList()
    if (!day.nextAthanAt.isAfter(now)) return emptyList()
    val next = firstAllowedHourAfter(day.nextAthanAt, zone, medicineSlots) ?: return emptyList()
    return if (next.isAfter(now)) listOf(next) else emptyList()
}

/** Athkar stays quiet this minute. Medicine still speaks. */
internal fun athkarYieldsToMedicine(
    medicineEnabled: Boolean,
    slots: List<MedicineSlot>,
    at: Instant,
    zone: ZoneId,
    graceMinutes: Long = MEDICINE_GRACE_MINUTES,
): Boolean = medicineEnabled && isMedicineMinute(slots, at, zone, graceMinutes)

internal fun shouldPlayAthkar(
    athkarEnabled: Boolean,
    inWindow: Boolean,
    athanPlaying: Boolean,
    medicinePlaying: Boolean,
    athanMinute: Boolean,
    medicineDue: Boolean,
): Boolean = athkarEnabled &&
    inWindow &&
    !athanPlaying &&
    !medicinePlaying &&
    !athanMinute &&
    !medicineDue

internal fun shouldStartMedicineFromAthkar(
    medicineDue: Boolean,
    athanPlaying: Boolean,
    athanMinute: Boolean,
): Boolean = medicineDue && !athanPlaying && !athanMinute

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
    medicineSlots: List<MedicineSlot>,
): List<Instant> {
    var cursor = firstHourAfter(fajr, zone).atZone(zone)
    val end = isha.atZone(zone)
    val hours = mutableListOf<Instant>()
    var guard = 0
    while (cursor.isBefore(end) && guard < 24) {
        val instant = cursor.toInstant()
        if (instant.isAfter(now) &&
            isAthkarAllowedHour(cursor.hour) &&
            !isAthanMinute(athanTimes, instant, zone) &&
            !isMedicineMinute(medicineSlots, instant, zone, graceMinutes = 0)
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

private fun firstAllowedHourAfter(
    instant: Instant,
    zone: ZoneId,
    medicineSlots: List<MedicineSlot>,
): Instant? {
    var cursor = firstHourAfter(instant, zone).atZone(zone)
    var guard = 0
    while (guard < 24) {
        val at = cursor.toInstant()
        if (isAthkarAllowedHour(cursor.hour) &&
            !isMedicineMinute(medicineSlots, at, zone, graceMinutes = 0)
        ) {
            return at
        }
        cursor = cursor.plusHours(1)
        guard++
    }
    return null
}
