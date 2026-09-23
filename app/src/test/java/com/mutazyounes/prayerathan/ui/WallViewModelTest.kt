package com.mutazyounes.prayerathan.ui

import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WallViewModelTest {

    @Test
    fun nightBlackoutHours() {
        // 11 PM to 4 AM (23:00 to 03:59)
        assertTrue(WallViewModel.isNightBlackoutWindow(23))
        assertTrue(WallViewModel.isNightBlackoutWindow(0))
        assertTrue(WallViewModel.isNightBlackoutWindow(1))
        assertTrue(WallViewModel.isNightBlackoutWindow(2))
        assertTrue(WallViewModel.isNightBlackoutWindow(3))

        // 4 AM to 10:59 PM is awake
        assertFalse(WallViewModel.isNightBlackoutWindow(4))
        assertFalse(WallViewModel.isNightBlackoutWindow(5))
        assertFalse(WallViewModel.isNightBlackoutWindow(12))
        assertFalse(WallViewModel.isNightBlackoutWindow(20))
        assertFalse(WallViewModel.isNightBlackoutWindow(21))
        assertFalse(WallViewModel.isNightBlackoutWindow(22))
    }

    @Test
    fun playingPrayerKeepsOwnTimeNotNextAthan() {
        val maghrib = Instant.parse("2026-09-15T23:10:00Z")
        val isha = Instant.parse("2026-09-16T00:25:00Z")
        assertEquals(
            maghrib,
            WallViewModel.cellDisplayInstant(
                instantAt = maghrib,
                name = PrayerName.MAGHRIB,
                playingName = PrayerName.MAGHRIB,
                nextAthan = PrayerName.ISHA,
                nextAthanAt = isha,
            ),
        )
    }

    @Test
    fun afterIshaFajrTileShowsTomorrow() {
        val todayFajr = Instant.parse("2026-09-15T09:05:00Z")
        val tomorrowFajr = Instant.parse("2026-09-16T09:06:00Z")
        assertEquals(
            tomorrowFajr,
            WallViewModel.cellDisplayInstant(
                instantAt = todayFajr,
                name = PrayerName.FAJR,
                playingName = null,
                nextAthan = PrayerName.FAJR,
                nextAthanAt = tomorrowFajr,
            ),
        )
    }

    @Test
    fun settingsClockLabelKeepsAmPm() {
        val fajr = ZonedDateTime.of(2026, 9, 23, 5, 15, 0, 0, ZoneId.of("America/New_York"))
        assertEquals("5:15 AM", WallViewModel.formatPrayerClockLabel(fajr, twelveHour = true))
        val asr = ZonedDateTime.of(2026, 9, 23, 16, 42, 0, 0, ZoneId.of("America/New_York"))
        assertEquals("4:42 PM", WallViewModel.formatPrayerClockLabel(asr, twelveHour = true))
    }

    @Test
    fun wallPaletteIsDark() {
        assertEquals(DarkWallPalette.background, DarkWallPalette.background)
        assertTrue(DarkWallPalette.cellBackground.alpha == 0.0f)
    }
}
