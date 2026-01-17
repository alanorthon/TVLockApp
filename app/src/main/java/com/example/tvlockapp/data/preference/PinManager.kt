package com.example.tvlockapp.data.preference

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object PinManager {

    private const val PREF_NAME = "pin_prefs"
    private const val KEY_PIN = "pin"

    private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREF_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun setPin(context: Context, pin: String) {
        getEncryptedSharedPreferences(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getEncryptedSharedPreferences(context).getString(KEY_PIN, null)
    }

    fun isPinSet(context: Context): Boolean {
        return getPin(context) != null
    }
}