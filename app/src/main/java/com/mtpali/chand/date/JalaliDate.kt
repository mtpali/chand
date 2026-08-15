package com.mtpali.chand.date

import java.time.LocalDate
import java.time.ZoneId

data class JalaliDate(val year: Int, val month: Int, val day: Int, val dayOfWeek: String) {
    val monthName: String get() = MONTHS[month - 1]

    companion object {
        private val MONTHS = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )

        fun today(zoneId: ZoneId = ZoneId.systemDefault()): JalaliDate = fromGregorian(LocalDate.now(zoneId))

        fun fromGregorian(date: LocalDate): JalaliDate {
            var gy = date.year
            val gm = date.monthValue
            val gd = date.dayOfMonth
            val gDaysInMonthBefore = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

            val jyBase: Int
            if (gy > 1600) {
                jyBase = 979
                gy -= 1600
            } else {
                jyBase = 0
                gy -= 621
            }

            val gy2 = if (gm > 2) gy + 1 else gy
            var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + gDaysInMonthBefore[gm - 1]
            var jy = jyBase + 33 * (days / 12053)
            days %= 12053
            jy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                jy += (days - 1) / 365
                days = (days - 1) % 365
            }

            val jm: Int
            val jd: Int
            if (days < 186) {
                jm = 1 + days / 31
                jd = 1 + days % 31
            } else {
                jm = 7 + (days - 186) / 30
                jd = 1 + (days - 186) % 30
            }

            val weekday = when (date.dayOfWeek) {
                java.time.DayOfWeek.SATURDAY -> "شنبه"
                java.time.DayOfWeek.SUNDAY -> "یکشنبه"
                java.time.DayOfWeek.MONDAY -> "دوشنبه"
                java.time.DayOfWeek.TUESDAY -> "سه‌شنبه"
                java.time.DayOfWeek.WEDNESDAY -> "چهارشنبه"
                java.time.DayOfWeek.THURSDAY -> "پنجشنبه"
                java.time.DayOfWeek.FRIDAY -> "جمعه"
            }
            return JalaliDate(jy, jm, jd, weekday)
        }
    }
}
