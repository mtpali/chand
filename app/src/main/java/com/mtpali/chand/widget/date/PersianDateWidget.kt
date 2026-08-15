package com.mtpali.chand.widget.date

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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
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
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.WidgetPalette

class PersianDateWidget : GlanceAppWidget() {
    // Exact sizing lets us compensate for launchers such as MIUI that provide
    // a tall 2x2 host area. The visible card itself stays square.
    override val sizeMode: SizeMode = SizeMode.Exact

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
                .appWidgetBackground(),
            contentAlignment = Alignment.Center
        ) {
            // Using a drawable as an image gives us rounded corners on Android 11
            // and older, where Glance's runtime corner radius is not reliable.
            Image(
                provider = ImageProvider(R.drawable.widget_card_white),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.fillMaxSize()
            )

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(if (compact) 10.dp else 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!compact) {
                    Text(
                        date.dayOfWeek,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(GlanceModifier.height(3.dp))
                }

                Text(
                    PersianNumbers.digits(date.day),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (compact) 39.sp else 51.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(GlanceModifier.height(if (compact) 1.dp else 2.dp))

                Text(
                    if (compact) date.monthName else "${date.monthName} ${PersianNumbers.digits(date.year)}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (compact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
