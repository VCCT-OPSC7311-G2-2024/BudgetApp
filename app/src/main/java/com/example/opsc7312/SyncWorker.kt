package com.example.opsc7312

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = BudgetRepository(applicationContext)
        return try {
            repository.syncBudgets()
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Retry if the sync fails
        }
    }

    companion object {
        fun createSyncWorkRequest(): WorkRequest {
            return PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .build()
        }
    }
}
