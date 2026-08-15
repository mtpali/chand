package com.mtpali.chand.widget.combined

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mtpali.chand.work.PriceUpdateScheduler

class CombinedWidgetReceiver : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PriceUpdateScheduler.schedule(context)
        PriceUpdateScheduler.enqueueNow(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CombinedWidgetRenderer.update(context, appWidgetManager, appWidgetIds)
        PriceUpdateScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        CombinedWidgetRenderer.update(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            CombinedWidgetRenderer.updateAll(context)
            PriceUpdateScheduler.enqueueNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.mtpali.chand.action.REFRESH_COMBINED_WIDGET"
    }
}
