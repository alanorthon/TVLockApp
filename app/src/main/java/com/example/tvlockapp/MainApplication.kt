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

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        val dailyResetRequest = PeriodicWorkRequestBuilder<ResetAppLimitsWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reset_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyResetRequest
        )
    }
}