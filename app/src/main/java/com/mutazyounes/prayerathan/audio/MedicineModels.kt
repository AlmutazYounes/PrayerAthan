package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.R
import java.time.DayOfWeek
import java.util.UUID

enum class MedicineVoice {
    ARABIC,
    ENGLISH,
    ;

    val rawRes: Int
        get() = when (this) {
            ARABIC -> R.raw.medicine_ar
            ENGLISH -> R.raw.medicine_en
        }

    val wallPrimary: String
        get() = when (this) {
            ARABIC -> "حان وقت الدواء"
            ENGLISH -> "Take your medicine"
        }

    val wallSecondary: String
        get() = when (this) {
            ARABIC -> "Take your medicine"
            ENGLISH -> "حان وقت الدواء"
        }

    companion object {
        fun fromStored(raw: String?): MedicineVoice =
            entries.firstOrNull { it.name == raw } ?: ARABIC
    }
}

data class MedicineSlot(
    val id: String,
    val hour: Int,
    val minute: Int,
    val days: Set<DayOfWeek>,
) {
    init {
        require(hour in 0..23) { "hour out of range" }
        require(minute in 0..59) { "minute out of range" }
        require(days.isNotEmpty()) { "pick at least one day" }
    }

    companion object {
        const val MAX_SLOTS = 6

        fun create(
            hour: Int = 8,
            minute: Int = 0,
            days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        ): MedicineSlot = MedicineSlot(
            id = UUID.randomUUID().toString().take(8),
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
            days = days.ifEmpty { DayOfWeek.entries.toSet() },
        )
    }
}
