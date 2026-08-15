package com.mtpali.chand.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mtpali.chand.data.DollarRepository
import com.mtpali.chand.widget.dollar.DollarWidget
import com.mtpali.chand.widget.date.PersianDateWidget
import kotlinx.coroutines.runBlocking

class PriceUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        return runCatching {
            DollarRepository(applicationContext).refresh()
            runBlocking {
                DollarWidget().updateAll(applicationContext)
                PersianDateWidget().updateAll(applicationContext)
            }
            Result.success()
        }.getOrElse {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
