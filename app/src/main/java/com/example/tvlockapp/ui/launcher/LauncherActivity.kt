package com.example.tvlockapp.ui.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.tvlockapp.ui.main.MainActivity

class LauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}