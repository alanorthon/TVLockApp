package com.example.tvlockapp

import android.app.Application
import androidx.work.*
import com.example.tvlockapp.worker.ResetAppLimitsWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupDailyResetWorker()
    }

    private fun setupDailyResetWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (midnight.before(now)) {
            midnight.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = midnight.timeInMillis - now.timeInMillis

        val dailyResetRequest = PeriodicWorkRequestBuilder<ResetAppLimitsWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reset_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyResetRequest
        )
    }
}