package com.chand.mobiletina.util

import com.chand.mobiletina.data.DollarRate

/** Shared status wording for the app, dollar widget and combined widget. */
object DollarChangeText {
    fun forRate(rate: DollarRate): String = when {
        rate.deltaToman > 0 -> "↑${PersianNumbers.grouped(rate.deltaToman)}"
        rate.deltaToman < 0 -> "↓${PersianNumbers.grouped(-rate.deltaToman)}"
        else -> "بدون تغییر"
    }
}
