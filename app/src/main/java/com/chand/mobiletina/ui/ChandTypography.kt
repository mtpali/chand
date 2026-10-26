package com.chand.mobiletina.ui

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.chand.mobiletina.R

/** One set of bundled font files, shared by Compose, platform text and bitmap widgets. */
object ChandFonts {
    val family = FontFamily(
        Font(R.font.vazirmatn_regular, FontWeight.Normal),
        Font(R.font.vazirmatn_medium, FontWeight.Medium),
        Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
        Font(R.font.vazirmatn_bold, FontWeight.Bold)
    )

    @Volatile private var regularCache: Typeface? = null
    @Volatile private var boldCache: Typeface? = null

    fun regular(context: Context): Typeface = regularCache ?: synchronized(this) {
        regularCache ?: context.applicationContext.resources.getFont(R.font.vazirmatn_regular)
            .also { regularCache = it }
    }

    fun bold(context: Context): Typeface = boldCache ?: synchronized(this) {
        boldCache ?: context.applicationContext.resources.getFont(R.font.vazirmatn_bold)
            .also { boldCache = it }
    }
}

private val defaults = Typography()

val ChandTypography = Typography(
    displayLarge = defaults.displayLarge.copy(fontFamily = ChandFonts.family),
    displayMedium = defaults.displayMedium.copy(fontFamily = ChandFonts.family),
    displaySmall = defaults.displaySmall.copy(fontFamily = ChandFonts.family),
    headlineLarge = defaults.headlineLarge.copy(fontFamily = ChandFonts.family),
    headlineMedium = defaults.headlineMedium.copy(fontFamily = ChandFonts.family),
    headlineSmall = defaults.headlineSmall.copy(fontFamily = ChandFonts.family),
    titleLarge = defaults.titleLarge.copy(fontFamily = ChandFonts.family),
    titleMedium = defaults.titleMedium.copy(fontFamily = ChandFonts.family),
    titleSmall = defaults.titleSmall.copy(fontFamily = ChandFonts.family),
    bodyLarge = defaults.bodyLarge.copy(fontFamily = ChandFonts.family),
    bodyMedium = defaults.bodyMedium.copy(fontFamily = ChandFonts.family),
    bodySmall = defaults.bodySmall.copy(fontFamily = ChandFonts.family),
    labelLarge = defaults.labelLarge.copy(fontFamily = ChandFonts.family),
    labelMedium = defaults.labelMedium.copy(fontFamily = ChandFonts.family),
    labelSmall = defaults.labelSmall.copy(fontFamily = ChandFonts.family)
)
