package com.chand.mobiletina.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DollarRateTest {
    @Test
    fun unchangedQuoteResetsMovementWhenSwitchingBetweenSources() {
        val previous = DollarRate(270_500L, 270_800L, 1L, "Hansha")
        val fallback = previous.withFetchedPrice(270_500L, 2L, "TGJU Web")
        assertEquals(270_500L, fallback.previousToman)
        assertEquals(0L, fallback.deltaToman)
        assertEquals(2L, fallback.updatedAtMillis)
        assertEquals("TGJU Web", fallback.source)
        val primary = fallback.withFetchedPrice(270_700L, 3L, "Hansha")
        assertEquals(200L, primary.deltaToman)
    }

    @Test
    fun repeatedPriceHasZeroMovementAfterEitherRiseOrFall() {
        val first = DollarRate(100_000L, 100_000L, 1L, "test")
        assertEquals(0L, first.deltaToman)

        val risen = first.withFetchedPrice(101_000L, 2L, "test")
        assertEquals(1_000L, risen.deltaToman)

        val unchanged = risen.withFetchedPrice(101_000L, 3L, "test")
        assertEquals(101_000L, unchanged.previousToman)
        assertEquals(0L, unchanged.deltaToman)
        assertEquals(3L, unchanged.updatedAtMillis)

        val fallen = unchanged.withFetchedPrice(100_500L, 4L, "test")
        assertEquals(-500L, fallen.deltaToman)
        val stillUnchanged = fallen.withFetchedPrice(100_500L, 5L, "test")
        assertEquals(100_500L, stillUnchanged.previousToman)
        assertEquals(0L, stillUnchanged.deltaToman)
        assertEquals(0L, stillUnchanged.withFetchedPrice(100_500L, 6L, "test").deltaToman)
    }
}
