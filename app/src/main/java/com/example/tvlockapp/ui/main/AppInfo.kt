package com.example.tvlockapp.ui.main

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val dailyLimit: Long, // in milliseconds
    val dailyUsage: Long  // in milliseconds
) {
    fun getDailyLimitInMinutes(): Long {
        return dailyLimit / (60 * 1000)
    }
    
    fun getDailyUsageInMinutes(): Long {
        return dailyUsage / (60 * 1000)
    }
    
    fun getRemainingTimeInMinutes(): Long {
        val remaining = dailyLimit - dailyUsage
        return if (remaining > 0) remaining / (60 * 1000) else 0
    }
    
    fun isLimitExceeded(): Boolean {
        return dailyUsage >= dailyLimit && dailyLimit > 0
    }
    
    fun hasLimit(): Boolean {
        return dailyLimit > 0
    }
}