package com.example.tvlockapp.ui.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R
import com.example.tvlockapp.data.preference.PinManager
import com.example.tvlockapp.service.AppUsageService
import com.example.tvlockapp.ui.pin.PinActivity
import com.example.tvlockapp.ui.pin.PinMode

class MainActivity : FragmentActivity() {

    private val pinVerificationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            initializeApp()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkPermissions()) {
            return
        }

        startMonitoringService()

        if (!PinManager.isPinSet(this)) {
            val intent = Intent(this, PinActivity::class.java).apply {
                putExtra(PinMode.EXTRA_MODE, PinMode.SETUP)
            }
            startActivity(intent)
            finish()
            return
        }

        val intent = Intent(this, PinActivity::class.java).apply {
            putExtra(PinMode.EXTRA_MODE, PinMode.VERIFY)
        }
        pinVerificationLauncher.launch(intent)
    }

    private fun initializeApp() {
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, MainFragment())
            .commitNow()
    }

    private fun startMonitoringService() {
        val serviceIntent = Intent(this, AppUsageService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun checkPermissions(): Boolean {
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return false
        }

        return true
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
        Toast.makeText(this, "Предоставьте доступ к статистике", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "Разрешите отображение поверх других окон", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
