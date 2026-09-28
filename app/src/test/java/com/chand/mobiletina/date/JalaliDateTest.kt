package com.chand.mobiletina.date

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class JalaliDateTest {
    @Test
    fun convertsAugust15_2026() {
        val date = JalaliDate.fromGregorian(LocalDate.of(2026, 8, 15))
        assertEquals(1405, date.year)
        assertEquals(5, date.month)
        assertEquals(24, date.day)
        assertEquals("شنبه", date.dayOfWeek)
    }

    @Test
    fun convertsNowruz2025() {
        val date = JalaliDate.fromGregorian(LocalDate.of(2025, 3, 21))
        assertEquals(1404, date.year)
        assertEquals(1, date.month)
        assertEquals(1, date.day)
    }
}
