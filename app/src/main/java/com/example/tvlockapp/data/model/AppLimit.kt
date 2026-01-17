package com.example.tvlockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    var dailyUsageLimit: Long,
    var isBlocked: Boolean = false
)