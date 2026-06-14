package com.example.tvlockapp.ui.lock

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R

class LockActivity : FragmentActivity() {

    private lateinit var appIcon: ImageView
    private lateinit var appName: TextView
    private lateinit var messageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        appIcon = findViewById(R.id.blocked_app_icon)
        appName = findViewById(R.id.blocked_app_name)
        messageText = findViewById(R.id.lock_message)

        val blockedPackage = intent.getStringExtra("blocked_package")
        if (blockedPackage != null) {
            setupBlockedAppInfo(blockedPackage)
        }

        messageText.text = "Приложение заблокировано. Нажмите OK для возврата на главный экран."

        findViewById<Button>(R.id.ok_button).setOnClickListener {
            goToHome()
        }
    }

    private fun setupBlockedAppInfo(packageName: String) {
        try {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appLabel = packageManager.getApplicationLabel(appInfo).toString()
            val appIconDrawable = packageManager.getApplicationIcon(appInfo)

            appName.text = appLabel
            appIcon.setImageDrawable(appIconDrawable)
        } catch (e: PackageManager.NameNotFoundException) {
            appName.text = "Неизвестное приложение"
        }
    }

    private fun goToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        finish()
    }

    override fun onBackPressed() {
        goToHome()
    }
}