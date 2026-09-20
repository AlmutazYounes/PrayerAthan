package com.mutazyounes.prayerathan.engine

data class PrayerOffsets(
    val minutes: Map<PrayerName, Int> = emptyMap(),
) {
    fun minutesOf(name: PrayerName): Int = clamp(minutes[name] ?: 0)

    fun with(name: PrayerName, value: Int): PrayerOffsets {
        val next = ALL.associateWith { minutesOf(it) }.toMutableMap()
        next[name] = clamp(value)
        return PrayerOffsets(next)
    }

    fun fingerprint(): List<Int> = ALL.map { minutesOf(it) }

    companion object {
        const val MIN = -60
        const val MAX = 60
        val ZERO = PrayerOffsets()
        val ALL: List<PrayerName> = enumValues<PrayerName>().toList()

        fun clamp(minutes: Int): Int = minutes.coerceIn(MIN, MAX)
    }
}
