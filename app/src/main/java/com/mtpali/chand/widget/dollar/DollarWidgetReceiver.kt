package com.mtpali.chand.widget.dollar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mtpali.chand.widget.WidgetRenderer
import com.mtpali.chand.work.PriceUpdateScheduler

class DollarWidgetReceiver : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Do not make scheduling part of the critical launcher rendering path.
        PriceUpdateScheduler.schedule(context)
        PriceUpdateScheduler.enqueueNow(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Always render the cached value first. Even if networking/WorkManager is unavailable,
        // the launcher still receives a valid bitmap and never gets a blank provider.
        WidgetRenderer.updateDollar(context, appWidgetManager, appWidgetIds)
        PriceUpdateScheduler.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetRenderer.updateDollar(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            // Show the cached card immediately, then request a network refresh.
            WidgetRenderer.updateDollarAll(context)
            PriceUpdateScheduler.enqueueNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.mtpali.chand.action.REFRESH_DOLLAR_WIDGET"
    }
}
