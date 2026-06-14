package com.example.tvlockapp.ui.pin

enum class PinMode {
    SETUP,
    VERIFY,
    CONFIRM,
    CHANGE;

    companion object {
        const val EXTRA_MODE = "pin_mode"
    }
}
