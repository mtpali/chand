package com.mtpali.chand.widget.dollar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.mtpali.chand.R
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.data.DollarRate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.WidgetPalette
import com.mtpali.chand.work.PriceUpdateScheduler

class DollarWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = AppPreferences(context)
        val rate = preferences.cachedDollarRate()
        val theme = preferences.widgetTheme()
        provideContent {
            GlanceTheme(colors = WidgetPalette.colors(theme)) {
                DollarContent(rate)
            }
        }
    }
}

@Composable
private fun DollarContent(rate: DollarRate?) {
    val available = LocalSize.current
    val shortestSide = minOf(available.width, available.height)
    val cardSide = if (shortestSide > 84.dp) shortestSide - 4.dp else shortestSide
    val compact = cardSide < 108.dp

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(cardSide)
                .appWidgetBackground()
                .clickable(actionRunCallback<RefreshDollarAction>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_card_white),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.fillMaxSize()
            )

            if (compact) CompactDollar(rate) else FullDollar(rate)
        }
    }
}

@Composable
private fun CompactDollar(rate: DollarRate?) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.us_flag_round),
            contentDescription = "US Dollar",
            modifier = GlanceModifier.size(31.dp)
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            rate?.let { PersianNumbers.grouped(it.priceToman) } ?: "—",
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun FullDollar(rate: DollarRate?) {
    val delta = rate?.deltaToman ?: 0L
    val deltaText = when {
        delta > 0 -> "↑ ${PersianNumbers.grouped(delta)}"
        delta < 0 -> "↓ ${PersianNumbers.grouped(-delta)}"
        rate != null -> "بدون تغییر"
        else -> "در حال بروزرسانی"
    }
    val deltaColor = when {
        delta > 0 -> GlanceTheme.colors.error
        delta < 0 -> GlanceTheme.colors.primary
        else -> GlanceTheme.colors.onSurfaceVariant
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(13.dp),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.us_flag_round),
                contentDescription = "US Dollar",
                modifier = GlanceModifier.size(32.dp)
            )

            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "دلار آمریکا",
                    modifier = GlanceModifier.fillMaxWidth(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Right
                    )
                )
                Text(
                    "USD",
                    modifier = GlanceModifier.fillMaxWidth(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Right
                    )
                )
            }
        }

        Spacer(GlanceModifier.height(9.dp))

        Text(
            deltaText,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = deltaColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Left
            )
        )

        Spacer(GlanceModifier.height(1.dp))

        Text(
            rate?.let { PersianNumbers.grouped(it.priceToman) } ?: "—",
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Left
            )
        )
    }
}

class RefreshDollarAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        PriceUpdateScheduler.enqueueNow(context)
    }
}
