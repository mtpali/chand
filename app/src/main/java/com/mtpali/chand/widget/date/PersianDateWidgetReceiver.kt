package com.mtpali.chand.widget.date

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.mtpali.chand.widget.WidgetRenderer

class PersianDateWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetRenderer.updateDate(context, appWidgetManager, appWidgetIds)
    }
}
