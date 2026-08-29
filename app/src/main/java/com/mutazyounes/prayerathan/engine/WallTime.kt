package com.mutazyounes.prayerathan.engine

import java.time.Instant

/**
 * In-app clock correction. [correctionMs] is NTP minus the device clock.
 * Positive means the tablet is slow. Alarms still use AlarmManager's device clock,
 * so the trigger is shifted the other way.
 */
object WallTime {
    fun now(systemEpochMilli: Long, correctionMs: Long): Instant =
        Instant.ofEpochMilli(systemEpochMilli + correctionMs)

    fun alarmEpochMilli(civilEpochMilli: Long, correctionMs: Long): Long =
        civilEpochMilli - correctionMs

    fun millisUntilNextSecond(nowEpochMilli: Long): Long =
        (1000L - (nowEpochMilli % 1000L)).coerceAtLeast(1L)
}

interface WallClock {
    fun now(): Instant
    fun alarmEpochMilli(civil: Instant): Long
    fun syncIfDue(): Boolean
}

object SystemWallClock : WallClock {
    override fun now(): Instant = Instant.now()
    override fun alarmEpochMilli(civil: Instant): Long = civil.toEpochMilli()
    override fun syncIfDue(): Boolean = false
}
