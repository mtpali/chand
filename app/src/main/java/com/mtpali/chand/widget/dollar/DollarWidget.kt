package com.mtpali.chand.widget.dollar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
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
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 110.dp), DpSize(180.dp, 180.dp))
    )

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
    val compact = LocalSize.current.width < 150.dp || LocalSize.current.height < 150.dp
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(R.dimen.widget_corner_radius)
            .appWidgetBackground()
            .clickable(actionRunCallback<RefreshDollarAction>())
            .padding(if (compact) 10.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                "🇺🇸",
                style = TextStyle(fontSize = if (compact) 24.sp else 30.sp, textAlign = TextAlign.Center)
            )
            if (!compact) {
                Text(
                    "دلار آمریکا",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    "USD",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(GlanceModifier.height(7.dp))
                val delta = rate?.deltaToman ?: 0L
                val deltaText = when {
                    delta > 0 -> "↑ ${PersianNumbers.grouped(delta)}"
                    delta < 0 -> "↓ ${PersianNumbers.grouped(-delta)}"
                    rate != null -> "بدون تغییر"
                    else -> "برای بروزرسانی لمس کنید"
                }
                val deltaColor = when {
                    delta > 0 -> GlanceTheme.colors.error
                    delta < 0 -> GlanceTheme.colors.primary
                    else -> GlanceTheme.colors.onSurfaceVariant
                }
                Text(
                    deltaText,
                    style = TextStyle(
                        color = deltaColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
            }
            Text(
                rate?.let { PersianNumbers.grouped(it.priceToman) } ?: "—",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = if (compact) 25.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                "تومان",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = if (compact) 10.sp else 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class RefreshDollarAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        PriceUpdateScheduler.enqueueNow(context)
    }
}
