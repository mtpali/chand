package com.chand.mobiletina.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DollarRateTest {
    @Test
    fun repeatedPriceKeepsLastNonzeroMovementUntilPriceChangesAgain() {
        val first = DollarRate(100_000L, 100_000L, 1L, "test")
        assertEquals(0L, first.deltaToman)

        val risen = first.withFetchedPrice(101_000L, 2L, "test")
        assertEquals(1_000L, risen.deltaToman)

        val unchanged = risen.withFetchedPrice(101_000L, 3L, "test")
        assertEquals(100_000L, unchanged.previousToman)
        assertEquals(1_000L, unchanged.deltaToman)
        assertEquals(3L, unchanged.updatedAtMillis)

        val fallen = unchanged.withFetchedPrice(100_500L, 4L, "test")
        assertEquals(-500L, fallen.deltaToman)
        assertEquals(-500L, fallen.withFetchedPrice(100_500L, 5L, "test").deltaToman)
    }
}
