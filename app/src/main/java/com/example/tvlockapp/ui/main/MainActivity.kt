package com.example.tvlockapp.ui.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R
import com.example.tvlockapp.data.preference.PinManager
import com.example.tvlockapp.service.AppUsageService
import com.example.tvlockapp.ui.pin.PinActivity

class MainActivity : FragmentActivity() {

    override fun onBackPressed() {
        // Instead of closing, move the task to the background
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
            return
        }

        // Start the usage monitoring service
        val serviceIntent = Intent(this, AppUsageService::class.java)
        startService(serviceIntent)

        // Check if PIN is set
        if (!PinManager.isPinSet(this)) {
            // First time launch - set PIN
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("mode", "setup")
            startActivity(intent)
            finish()
            return
        }

        // PIN is set, verify it
        val intent = Intent(this, PinActivity::class.java)
        intent.putExtra("mode", "verify")
        startActivityForResult(intent, PIN_VERIFICATION_REQUEST)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PIN_VERIFICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // PIN verified successfully
                initializeApp()
            } else {
                // PIN verification failed
                finish()
            }
        }
    }
    
    private fun initializeApp() {
        setContentView(R.layout.activity_main)

        // Load the main fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, MainFragment())
            .commitNow()
    }
    
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val PIN_VERIFICATION_REQUEST = 1001
    }
}