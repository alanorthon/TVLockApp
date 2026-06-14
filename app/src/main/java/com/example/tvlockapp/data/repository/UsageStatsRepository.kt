package com.example.tvlockapp.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

class UsageStatsRepository(context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getUsageToday(packageName: String): Long {
        val usageMap = getUsageMapForToday()
        return usageMap[packageName] ?: 0
    }

    fun getUsageMapForToday(): Map<String, Long> {
        val (startOfDay, now) = getTodayRange()
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startOfDay, now
        )
        return statsList.associate { it.packageName to it.totalTimeInForeground }
    }

    fun getForegroundApp(): String? {
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 30_000, now)
        val event = android.app.usage.UsageEvents.Event()
        var lastForeground: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForeground = event.packageName
            }
        }

        if (lastForeground == null) {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - 60_000, now
            )
            lastForeground = stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        }

        return lastForeground
    }

    companion object {
        fun getTodayRange(): Pair<Long, Long> {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return Pair(calendar.timeInMillis, System.currentTimeMillis())
        }
    }
}
