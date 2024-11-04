package com.example.opsc7312

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.opsc7312.BudgetRepository

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
}
