package com.example.tvlockapp.ui.pin

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R

class PinActivity : FragmentActivity() {

    override fun onBackPressed() {
        // Do nothing to prevent user from going back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        
        val mode = intent.getStringExtra("mode") ?: "setup"
        
        if (savedInstanceState == null) {
            val fragment = PinFragment.newInstance(mode)
            supportFragmentManager.beginTransaction()
                .replace(R.id.pin_container, fragment)
                .commitNow()
        }
    }
}