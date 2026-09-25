package com.mutazyounes.prayerathan.audio

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal const val MORNING_ATHKAR_START_HOUR = 8
internal const val MORNING_ATHKAR_START_MINUTE = 5
internal const val MORNING_ATHKAR_INTERVAL_MINUTES = 5

data class MorningAthkarSlot(
    val at: Instant,
    val index: Int,
)

internal fun morningAthkarLocalTimes(clipCount: Int): List<LocalTime> {
    val count = clipCount.coerceAtLeast(0)
    return (0 until count).map { index ->
        LocalTime.of(MORNING_ATHKAR_START_HOUR, MORNING_ATHKAR_START_MINUTE)
            .plusMinutes(MORNING_ATHKAR_INTERVAL_MINUTES.toLong() * index)
    }
}

internal fun remainingMorningAthkarAlarms(
    now: Instant,
    zone: ZoneId,
    clipCount: Int = MorningAthkarClip.count,
    athanTimes: List<Instant> = emptyList(),
    medicineSlots: List<MedicineSlot> = emptyList(),
): List<MorningAthkarSlot> {
    if (clipCount <= 0) return emptyList()
    val today = now.atZone(zone).toLocalDate()
    val todaySlots = morningSlotsForDate(today, zone, clipCount, athanTimes, medicineSlots)
        .filter { it.at.isAfter(now) }
    if (todaySlots.isNotEmpty()) return todaySlots
    val tomorrow = today.plusDays(1)
    return morningSlotsForDate(tomorrow, zone, clipCount, athanTimes, medicineSlots)
        .filter { it.at.isAfter(now) }
}

internal fun shouldPlayMorningAthkar(
    morningEnabled: Boolean,
    athanPlaying: Boolean,
    medicinePlaying: Boolean,
    athanMinute: Boolean,
    medicineDue: Boolean,
): Boolean = morningEnabled &&
    !athanPlaying &&
    !medicinePlaying &&
    !athanMinute &&
    !medicineDue

private fun morningSlotsForDate(
    date: LocalDate,
    zone: ZoneId,
    clipCount: Int,
    athanTimes: List<Instant>,
    medicineSlots: List<MedicineSlot>,
): List<MorningAthkarSlot> {
    return morningAthkarLocalTimes(clipCount).mapIndexedNotNull { index, localTime ->
        val at = date.atTime(localTime).atZone(zone).toInstant()
        if (isAthanMinute(athanTimes, at, zone)) return@mapIndexedNotNull null
        if (isMedicineMinute(medicineSlots, at, zone, graceMinutes = 0)) return@mapIndexedNotNull null
        MorningAthkarSlot(at = at, index = index)
    }
}
