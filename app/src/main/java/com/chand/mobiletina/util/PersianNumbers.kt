package com.chand.mobiletina.util

import java.text.NumberFormat
import java.util.Locale

object PersianNumbers {
    private val formatter = NumberFormat.getIntegerInstance(Locale.forLanguageTag("fa-IR"))

    fun digits(value: Int): String = value.toString().toPersianDigits()
    fun digits(value: Long): String = value.toString().toPersianDigits()

    @Synchronized
    fun grouped(value: Long): String = formatter.format(value)

    fun String.toPersianDigits(): String = buildString(length) {
        for (char in this@toPersianDigits) {
            append(
                when (char) {
                    '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'
                    '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'
                    else -> char
                }
            )
        }
    }
}
