package com.mutazyounes.prayerathan.audio

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class MedicineAlarm(
    val slotId: String,
    val at: Instant,
)

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
): Boolean {
    val local = now.atZone(zone)
    return slots.any { slot ->
        local.dayOfWeek in slot.days &&
            local.hour == slot.hour &&
            local.minute == slot.minute
    }
}

internal fun medicineSlotFor(
    slots: List<MedicineSlot>,
    now: Instant,
    zone: ZoneId,
): MedicineSlot? {
    val local = now.atZone(zone)
    return slots.firstOrNull { slot ->
        local.dayOfWeek in slot.days &&
            local.hour == slot.hour &&
            local.minute == slot.minute
    }
}
