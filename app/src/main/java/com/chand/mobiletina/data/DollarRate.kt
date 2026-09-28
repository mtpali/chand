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
            // A repeated quote must not erase the last actual price movement.
            previousToman = if (newPriceToman == priceToman) previousToman else priceToman,
            updatedAtMillis = fetchedAtMillis,
            source = newSource
        )
}
