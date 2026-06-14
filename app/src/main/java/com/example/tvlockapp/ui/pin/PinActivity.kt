package com.example.tvlockapp.ui.pin

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tvlockapp.R

class PinActivity : FragmentActivity() {

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val mode = (intent.getSerializableExtra(PinMode.EXTRA_MODE) as? PinMode) ?: PinMode.SETUP

        if (savedInstanceState == null) {
            val fragment = PinFragment.newInstance(mode)
            supportFragmentManager.beginTransaction()
                .replace(R.id.pin_container, fragment)
                .commitNow()
        }
    }
}