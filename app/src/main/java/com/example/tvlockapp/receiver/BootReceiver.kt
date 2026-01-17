package com.example.tvlockapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tvlockapp.data.preference.PinManager
import com.example.tvlockapp.service.AppUsageService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only start service if PIN is already set (app has been configured)
            if (PinManager.isPinSet(context)) {
                val launchIntent = Intent(context, com.example.tvlockapp.ui.launcher.LauncherActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }
}