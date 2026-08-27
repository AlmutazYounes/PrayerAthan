package com.mutazyounes.prayerathan.engine

import java.time.Instant
import java.time.LocalDate

data class PrayerInstant(
    val name: PrayerName,
    val at: Instant,
)

data class PrayerDay(
    val localDate: LocalDate,
    val times: List<PrayerInstant>,
    val nextAthan: PrayerName,
    val nextAthanAt: Instant,
)
