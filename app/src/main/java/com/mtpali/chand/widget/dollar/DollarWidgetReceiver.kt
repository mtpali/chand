package com.mtpali.chand.widget.dollar

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.mtpali.chand.work.PriceUpdateScheduler

class DollarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DollarWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PriceUpdateScheduler.schedule(context)
        PriceUpdateScheduler.enqueueNow(context)
    }
}
