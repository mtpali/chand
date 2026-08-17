package com.chand.mobiletina.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chand.mobiletina.data.DollarRepository
import com.chand.mobiletina.security.IntegrityGuard
import com.chand.mobiletina.widget.WidgetRenderer
import com.chand.mobiletina.widget.combined.CombinedWidgetRenderer

class PriceUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        if (!IntegrityGuard.verify(applicationContext)) return Result.failure()

        return runCatching {
            DollarRepository(applicationContext).refresh()
            WidgetRenderer.updateDollarAll(applicationContext)
            WidgetRenderer.updateDateAll(applicationContext)
            CombinedWidgetRenderer.updateAll(applicationContext)
            Result.success()
        }.getOrElse {
            // Keep the last cached value visible even if the network request failed.
            WidgetRenderer.updateDollarAll(applicationContext)
            WidgetRenderer.updateDateAll(applicationContext)
            CombinedWidgetRenderer.updateAll(applicationContext)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
