package com.mtpali.chand.widget.dollar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import com.mtpali.chand.security.IntegrityGuard
import com.mtpali.chand.widget.WidgetRenderer
import com.mtpali.chand.work.PriceUpdateScheduler

class DollarWidgetReceiver : AppWidgetProvider() {
    private fun allowed(context: Context): Boolean {
        if (IntegrityGuard.verify(context.applicationContext)) return true
        Process.killProcess(Process.myPid())
        return false
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        if (!allowed(context)) return
        PriceUpdateScheduler.schedule(context)
        PriceUpdateScheduler.enqueueNow(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        if (!allowed(context)) return
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
        if (!allowed(context)) return
        // Render from the options in this exact callback to avoid MIUI's delayed resize race.
        WidgetRenderer.updateDollar(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!allowed(context)) return
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            WidgetRenderer.updateDollarAll(context)
            PriceUpdateScheduler.enqueueNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.mtpali.chand.action.REFRESH_DOLLAR_WIDGET"
    }
}
