package com.mtpali.chand.widget.date

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
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
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.WidgetPalette

class PersianDateWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 110.dp), DpSize(180.dp, 180.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val date = JalaliDate.today()
        val theme = AppPreferences(context).widgetTheme()
        provideContent {
            GlanceTheme(colors = WidgetPalette.colors(theme)) {
                DateContent(date)
            }
        }
    }
}

@Composable
private fun DateContent(date: JalaliDate) {
    val compact = LocalSize.current.width < 150.dp || LocalSize.current.height < 150.dp
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(R.dimen.widget_corner_radius)
            .appWidgetBackground()
            .padding(if (compact) 10.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            if (!compact) {
                Text(
                    date.dayOfWeek,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(GlanceModifier.height(6.dp))
            }
            Text(
                PersianNumbers.digits(date.day),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = if (compact) 38.sp else 56.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(GlanceModifier.height(if (compact) 2.dp else 6.dp))
            Text(
                if (compact) date.monthName else "${date.monthName} ${PersianNumbers.digits(date.year)}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = if (compact) 13.sp else 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
