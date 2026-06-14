package com.example.tvlockapp.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tvlockapp.R
import com.example.tvlockapp.data.database.AppDatabase
import com.example.tvlockapp.data.repository.UsageStatsRepository
import com.example.tvlockapp.ui.lock.LockActivity
import kotlinx.coroutines.*
import java.util.*

class AppUsageService : Service() {

    private lateinit var database: AppDatabase
    private lateinit var usageStatsRepository: UsageStatsRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitoringJob: Job? = null
    private var lastCheckedDay: Int = 0

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "app_usage_service"
        private const val TAG = "AppUsageService"
        private const val CHECK_INTERVAL_MS = 10_000L
        private const val PREFS_NAME = "app_usage_service_prefs"
        private const val KEY_LAST_RESET_DAY = "last_reset_day"
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        usageStatsRepository = UsageStatsRepository(this)
        lastCheckedDay = getLastResetDay()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }

    private fun getLastResetDay(): Int {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_LAST_RESET_DAY, -1)
    }

    private fun saveLastResetDay(day: Int) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_RESET_DAY, day)
            .apply()
    }

    private fun startMonitoring() {
        monitoringJob = serviceScope.launch {
            while (isActive) {
                checkDayChange()
                checkAppUsage()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkDayChange() {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (currentDay != lastCheckedDay) {
            Log.d(TAG, "Day changed, resetting all blocks")
            database.appLimitDao().resetAllBlocks()
            lastCheckedDay = currentDay
            saveLastResetDay(currentDay)
        }
    }

    private suspend fun checkAppUsage() {
        val currentApp = getForegroundApp() ?: return

        val appLimit = database.appLimitDao().getAppLimit(currentApp)
        if (appLimit != null && appLimit.dailyUsageLimit > 0) {

            if (appLimit.isBlocked) {
                Log.d(TAG, "App $currentApp is already blocked today. Showing lock screen.")
                showLockScreen(currentApp)
                return
            }

            val usageToday = usageStatsRepository.getUsageToday(currentApp)

            if (usageToday >= appLimit.dailyUsageLimit) {
                Log.w(TAG, "Limit reached for $currentApp. Blocking.")
                database.appLimitDao().update(appLimit.copy(isBlocked = true))
                killAppProcesses(currentApp)
                showLockScreen(currentApp)
            }
        }
    }

    private fun getForegroundApp(): String? {
        val lastForeground = usageStatsRepository.getForegroundApp() ?: return null
        if (lastForeground == packageName) return null
        if (isLauncherApp(lastForeground)) return null
        return lastForeground
    }

    private fun isLauncherApp(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun killAppProcesses(packageName: String) {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(packageName)
    }

    private fun showLockScreen(packageName: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            putExtra("blocked_package", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Monitoring", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TV Lock")
            .setContentText("Мониторинг времени активен")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}
