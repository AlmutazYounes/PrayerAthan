package com.mutazyounes.prayerathan.engine

import java.time.Duration
import java.time.Instant

interface PrayerEngine {
    fun location(): SavedLocation
    fun day(now: Instant, location: SavedLocation = location()): PrayerDay
    fun clocks(now: Instant, location: SavedLocation = location()): WallClocks
    fun remainingToNext(now: Instant, location: SavedLocation = location()): Duration
}
