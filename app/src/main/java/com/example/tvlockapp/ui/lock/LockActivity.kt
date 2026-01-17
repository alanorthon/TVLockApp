package com.example.tvlockapp.ui.lock

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R
import com.example.tvlockapp.ui.main.MainActivity

class LockActivity : FragmentActivity() {

    private lateinit var appIcon: ImageView
    private lateinit var appName: TextView
    private lateinit var messageText: TextView
    private lateinit var okButton: Button
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)
        
        appIcon = findViewById(R.id.blocked_app_icon)
        appName = findViewById(R.id.blocked_app_name)
        messageText = findViewById(R.id.lock_message)
        okButton = findViewById(R.id.ok_button)
        
        val blockedPackage = intent.getStringExtra("blocked_package")
        
        if (blockedPackage != null) {
            setupBlockedAppInfo(blockedPackage)
        }
        
        okButton.setOnClickListener {
            goToHome()
        }

        messageText.text = "Приложение заблокировано. Вы будете перенаправлены через 5 секунд."

        handler.postDelayed({
            if (!isFinishing) {
                goToHome()
            }
        }, 5000)
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
        // Prevent going back to the blocked app
        goToHome()
    }
    


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}