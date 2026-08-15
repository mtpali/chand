package com.mtpali.chand.data

data class DollarRate(
    val priceToman: Long,
    val previousToman: Long,
    val updatedAtMillis: Long,
    val source: String
) {
    val deltaToman: Long get() = priceToman - previousToman
}
