package com.example.tvlockapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tvlockapp.R
import com.example.tvlockapp.data.database.AppDatabase
import com.example.tvlockapp.ui.lock.LockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var database: AppDatabase
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 10000L // Проверка каждые 10 секунд
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var lastCheckedDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "app_usage_service"
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkDayChange()
                checkAppUsage()
                handler.postDelayed(this, checkInterval)
            }
        })
    }

    private fun checkDayChange() {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (currentDay != lastCheckedDay) {
            Log.d("AppUsageService", "Day changed, resetting all blocks")
            serviceScope.launch {
                database.appLimitDao().resetAllBlocks()
                lastCheckedDay = currentDay
            }
        }
    }

    private fun checkAppUsage() {
        val currentApp = getForegroundApp() ?: return

        serviceScope.launch {
            val appLimit = database.appLimitDao().getAppLimit(currentApp)
            if (appLimit != null) {
                val usageToday = getUsageToday(currentApp)
                
                // Если лимит не 0 и использование превысило лимит
                if (appLimit.dailyUsageLimit > 0 && usageToday > appLimit.dailyUsageLimit) {
                    if (!appLimit.isBlocked) {
                        appLimit.isBlocked = true
                        database.appLimitDao().update(appLimit)
                    }
                    withContext(Dispatchers.Main) {
                        showLockScreen(currentApp)
                    }
                } else if (appLimit.isBlocked && usageToday <= appLimit.dailyUsageLimit) {
                    // На случай если день сменился, а статус в памяти остался
                    appLimit.isBlocked = false
                    database.appLimitDao().update(appLimit)
                }
            }
        }
    }

    private fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time)

        if (usageStatsList.isNullOrEmpty()) return null

        val sortedStats = usageStatsList.sortedByDescending { it.lastTimeUsed }
        val foregroundApp = sortedStats.firstOrNull()?.packageName

        if (foregroundApp == packageName || foregroundApp?.contains("launcher") == true) {
            return null
        }
        return foregroundApp
    }

    private fun getUsageToday(packageName: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, System.currentTimeMillis())
        return stats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0
    }

    private fun showLockScreen(packageName: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            putExtra("blocked_package", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, packageName.hashCode(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pendingIntent)
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
            .setContentText("Мониторинг времени")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}
