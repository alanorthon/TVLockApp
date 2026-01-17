package com.example.tvlockapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tvlockapp.data.database.AppDatabase

class ResetAppLimitsWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            database.appLimitDao().resetAllAppLimits()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}