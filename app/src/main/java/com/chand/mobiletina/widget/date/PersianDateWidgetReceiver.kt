package com.chand.mobiletina.widget.date

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.os.Process
import com.chand.mobiletina.security.IntegrityGuard
import com.chand.mobiletina.widget.WidgetRenderer

class PersianDateWidgetReceiver : AppWidgetProvider() {
    private fun allowed(context: Context): Boolean {
        if (IntegrityGuard.verify(context.applicationContext)) return true
        Process.killProcess(Process.myPid())
        return false
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        if (!allowed(context)) return
        WidgetRenderer.updateDate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (!allowed(context)) return
        // Use the options delivered with this resize event directly. Re-reading them from the
        // manager can race with MIUI's grid animation and make a medium widget jump larger later.
        WidgetRenderer.updateDate(context, appWidgetManager, appWidgetId, newOptions)
    }
}
