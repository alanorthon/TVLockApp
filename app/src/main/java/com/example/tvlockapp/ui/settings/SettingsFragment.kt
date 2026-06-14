package com.example.tvlockapp.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tvlockapp.R
import com.example.tvlockapp.ui.pin.PinActivity
import com.example.tvlockapp.ui.pin.PinMode

class SettingsFragment : PreferenceFragmentCompat() {

    private val verifyPinLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startActivity(
                Intent(requireContext(), PinActivity::class.java).apply {
                    putExtra(PinMode.EXTRA_MODE, PinMode.SETUP)
                }
            )
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("change_pin")?.setOnPreferenceClickListener {
            val intent = Intent(requireContext(), PinActivity::class.java).apply {
                putExtra(PinMode.EXTRA_MODE, PinMode.VERIFY)
            }
            verifyPinLauncher.launch(intent)
            true
        }
    }
}