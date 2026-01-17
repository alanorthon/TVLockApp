package com.example.tvlockapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tvlockapp.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_DATE_CHANGED) {
            val database = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                database.appLimitDao().resetAllBlocks()
            }
        }
    }
}