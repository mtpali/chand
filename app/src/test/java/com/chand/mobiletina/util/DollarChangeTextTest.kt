package com.chand.mobiletina.util

import com.chand.mobiletina.data.DollarRate
import org.junit.Assert.assertEquals
import org.junit.Test

class DollarChangeTextTest {
    @Test
    fun repeatedQuoteShowsUnchangedAfterRiseAndFall() {
        val initial = DollarRate(100_000L, 100_000L, 1L, "Hansha")
        val risen = initial.withFetchedPrice(101_000L, 2L, "Hansha")
        assertEquals("↑${PersianNumbers.grouped(1_000L)}", DollarChangeText.forRate(risen))
        val unchanged = risen.withFetchedPrice(101_000L, 3L, "AlanChand")
        assertEquals("بدون تغییر", DollarChangeText.forRate(unchanged))
        val fallen = unchanged.withFetchedPrice(100_500L, 4L, "TGJU Web")
        assertEquals("↓${PersianNumbers.grouped(500L)}", DollarChangeText.forRate(fallen))
        assertEquals("بدون تغییر", DollarChangeText.forRate(
            fallen.withFetchedPrice(100_500L, 5L, "Hansha")
        ))
    }

    @Test
    fun initialBaselineShowsUnchangedAndNeverAnEmptyStatus() {
        val initial = DollarRate(100_000L, 100_000L, 1L, "Hansha")
        assertEquals("بدون تغییر", DollarChangeText.forRate(initial))
    }
}
