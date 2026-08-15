package com.mtpali.chand.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.mtpali.chand.R
import com.mtpali.chand.data.AppPreferences
import com.mtpali.chand.data.DollarRate
import com.mtpali.chand.date.JalaliDate
import com.mtpali.chand.util.PersianNumbers
import com.mtpali.chand.widget.date.PersianDateWidgetReceiver
import com.mtpali.chand.widget.dollar.DollarWidgetReceiver

object WidgetRenderer {

    fun updateDateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, PersianDateWidgetReceiver::class.java))
        updateDate(context, manager, ids)
    }

    fun updateDate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val date = JalaliDate.today()
        ids.forEach { id ->
            manager.updateAppWidget(id, dateViews(context, date))
        }
    }

    private fun dateViews(context: Context, date: JalaliDate): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_date_ios).apply {
            setTextViewText(R.id.date_day_of_week, date.dayOfWeek)
            setTextViewText(R.id.date_day_number, PersianNumbers.digits(date.day))
            setTextViewText(
                R.id.date_full_date,
                "${date.monthName} ${PersianNumbers.digits(date.year)}"
            )
        }

    fun updateDollarAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, DollarWidgetReceiver::class.java))
        updateDollar(context, manager, ids)
    }

    fun updateDollar(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val rate = AppPreferences(context).cachedDollarRate()
        ids.forEach { id ->
            manager.updateAppWidget(id, dollarViews(context, rate))
        }
    }

    private fun dollarViews(context: Context, rate: DollarRate?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_dollar_ios)

        if (rate == null) {
            views.setTextViewText(R.id.dollar_delta, "لمس برای بروزرسانی")
            views.setTextColor(R.id.dollar_delta, Color.rgb(136, 136, 141))
            views.setTextViewText(R.id.dollar_price, "—")
        } else {
            val delta = rate.deltaToman
            val deltaText = when {
                delta > 0 -> "↑ ${PersianNumbers.grouped(delta)}"
                delta < 0 -> "↓ ${PersianNumbers.grouped(-delta)}"
                else -> "بدون تغییر"
            }
            val deltaColor = when {
                delta > 0 -> Color.rgb(183, 73, 73)
                delta < 0 -> Color.rgb(79, 135, 106)
                else -> Color.rgb(136, 136, 141)
            }
            views.setTextViewText(R.id.dollar_delta, deltaText)
            views.setTextColor(R.id.dollar_delta, deltaColor)
            views.setTextViewText(R.id.dollar_price, PersianNumbers.grouped(rate.priceToman))
        }

        val refreshIntent = Intent(context, DollarWidgetReceiver::class.java).apply {
            action = DollarWidgetReceiver.ACTION_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2401,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.dollar_widget_card, pendingIntent)
        return views
    }
}
