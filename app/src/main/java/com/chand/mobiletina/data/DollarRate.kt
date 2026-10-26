package com.chand.mobiletina.data

data class DollarRate(
    val priceToman: Long,
    val previousToman: Long,
    val updatedAtMillis: Long,
    val source: String
) {
    val deltaToman: Long get() = priceToman - previousToman

    fun withFetchedPrice(newPriceToman: Long, fetchedAtMillis: Long, newSource: String): DollarRate =
        DollarRate(
            priceToman = newPriceToman,
            // Compare each successful quote with the immediately preceding successful quote.
            previousToman = priceToman,
            updatedAtMillis = fetchedAtMillis,
            source = newSource
        )
}
