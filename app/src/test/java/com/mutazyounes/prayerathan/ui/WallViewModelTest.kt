package com.mutazyounes.prayerathan.ui

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
}
