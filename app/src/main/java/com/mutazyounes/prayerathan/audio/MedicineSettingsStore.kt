package com.mutazyounes.prayerathan.audio

import android.content.Context
import java.time.DayOfWeek

class MedicineSettingsStore(
    context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun enabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun voice(): MedicineVoice =
        MedicineVoice.fromStored(prefs.getString(KEY_VOICE, MedicineVoice.ARABIC.name))

    fun setVoice(voice: MedicineVoice) {
        prefs.edit().putString(KEY_VOICE, voice.name).apply()
    }

    fun slots(): List<MedicineSlot> {
        val raw = prefs.getStringSet(KEY_SLOTS, emptySet()) ?: emptySet()
        return raw.mapNotNull { decode(it) }.sortedWith(compareBy({ it.hour }, { it.minute }, { it.id }))
    }

    fun setSlots(slots: List<MedicineSlot>) {
        val capped = slots.take(MedicineSlot.MAX_SLOTS)
        prefs.edit().putStringSet(KEY_SLOTS, capped.map { encode(it) }.toSet()).apply()
    }

    fun upsertSlot(slot: MedicineSlot) {
        val next = slots().toMutableList()
        val index = next.indexOfFirst { it.id == slot.id }
        if (index >= 0) {
            next[index] = slot
        } else if (next.size < MedicineSlot.MAX_SLOTS) {
            next += slot
        }
        setSlots(next)
    }

    fun removeSlot(id: String) {
        setSlots(slots().filterNot { it.id == id })
    }

    companion object {
        private const val PREFS = "prayerathan_medicine"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_VOICE = "voice"
        private const val KEY_SLOTS = "slots"

        private val DAY_ORDER = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )

        internal fun encode(slot: MedicineSlot): String {
            val days = DAY_ORDER.joinToString("") { day ->
                if (day in slot.days) "1" else "0"
            }
            return listOf(slot.id, slot.hour, slot.minute, days).joinToString("|")
        }

        internal fun decode(raw: String): MedicineSlot? {
            val parts = raw.split("|")
            if (parts.size != 4) return null
            val id = parts[0].ifBlank { return null }
            val hour = parts[1].toIntOrNull() ?: return null
            val minute = parts[2].toIntOrNull() ?: return null
            val mask = parts[3]
            if (mask.length != 7) return null
            val days = DAY_ORDER.filterIndexed { index, _ -> mask.getOrNull(index) == '1' }.toSet()
            if (days.isEmpty()) return null
            return runCatching { MedicineSlot(id, hour, minute, days) }.getOrNull()
        }
    }
}
