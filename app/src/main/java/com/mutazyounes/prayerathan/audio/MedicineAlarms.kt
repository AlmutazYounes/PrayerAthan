package com.mutazyounes.prayerathan.audio

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class MedicineAlarm(
    val slotId: String,
    val at: Instant,
)

/** How late an alarm may still play after its scheduled minute. */
internal const val MEDICINE_GRACE_MINUTES = 3L

internal fun remainingMedicineAlarms(
    slots: List<MedicineSlot>,
    now: Instant,
    zone: ZoneId,
    lookAheadDays: Int = 7,
): List<MedicineAlarm> {
    if (slots.isEmpty()) return emptyList()
    val startDay = now.atZone(zone).toLocalDate()
    val alarms = mutableListOf<MedicineAlarm>()
    for (offset in 0 until lookAheadDays.coerceAtLeast(1)) {
        val day = startDay.plusDays(offset.toLong())
        for (slot in slots) {
            if (day.dayOfWeek !in slot.days) continue
            val at = ZonedDateTime.of(day, LocalTime.of(slot.hour, slot.minute), zone).toInstant()
            if (at.isAfter(now)) {
                alarms += MedicineAlarm(slot.id, at)
            }
        }
    }
    return alarms.sortedBy { it.at }
}

internal fun isMedicineMinute(
    slots: List<MedicineSlot>,
    now: Instant,
    zone: ZoneId,
    graceMinutes: Long = MEDICINE_GRACE_MINUTES,
): Boolean = medicineSlotFor(slots, now, zone, graceMinutes) != null

internal fun medicineSlotFor(
    slots: List<MedicineSlot>,
    now: Instant,
    zone: ZoneId,
    graceMinutes: Long = MEDICINE_GRACE_MINUTES,
): MedicineSlot? {
    if (slots.isEmpty()) return null
    val local = now.atZone(zone)
    return slots.firstOrNull { slot ->
        if (local.dayOfWeek !in slot.days) return@firstOrNull false
        val scheduled = ZonedDateTime.of(
            local.toLocalDate(),
            LocalTime.of(slot.hour, slot.minute),
            zone,
        )
        val minutesLate = ChronoUnit.MINUTES.between(scheduled, local)
        minutesLate in 0..graceMinutes
    }
}

internal fun medicineFireKey(
    slot: MedicineSlot,
    localDate: LocalDate,
): String = listOf(slot.id, localDate, slot.hour, slot.minute).joinToString("|")
