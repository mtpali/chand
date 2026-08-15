package com.mtpali.chand.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mtpali.chand.data.DollarRepository
import com.mtpali.chand.widget.WidgetRenderer

class PriceUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        return runCatching {
            DollarRepository(applicationContext).refresh()
            WidgetRenderer.updateDollarAll(applicationContext)
            WidgetRenderer.updateDateAll(applicationContext)
            Result.success()
        }.getOrElse {
            // Keep the last cached value visible even if the network request failed.
            WidgetRenderer.updateDollarAll(applicationContext)
            WidgetRenderer.updateDateAll(applicationContext)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
