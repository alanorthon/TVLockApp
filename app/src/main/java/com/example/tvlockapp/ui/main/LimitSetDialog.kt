package com.example.tvlockapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tvlockapp.R

class LimitSetDialog : DialogFragment() {

    private lateinit var appNameText: TextView
    private lateinit var hoursPicker: NumberPicker
    private lateinit var minutesPicker: NumberPicker
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var removeButton: Button
    
    private var packageName: String = ""
    private var appName: String = ""
    private var currentLimitMinutes: Long = 0
    
    private var onLimitSetListener: ((String, Long) -> Unit)? = null

    companion object {
        fun newInstance(packageName: String, appName: String, currentLimitMillis: Long): LimitSetDialog {
            val dialog = LimitSetDialog()
            val args = Bundle()
            args.putString("package_name", packageName)
            args.putString("app_name", appName)
            args.putLong("current_limit", currentLimitMillis / (60 * 1000)) // Convert to minutes
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_limit_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        packageName = arguments?.getString("package_name") ?: ""
        appName = arguments?.getString("app_name") ?: ""
        currentLimitMinutes = arguments?.getLong("current_limit") ?: 0
        
        appNameText = view.findViewById(R.id.app_name_text)
        hoursPicker = view.findViewById(R.id.hours_picker)
        minutesPicker = view.findViewById(R.id.minutes_picker)
        saveButton = view.findViewById(R.id.save_button)
        cancelButton = view.findViewById(R.id.cancel_button)
        removeButton = view.findViewById(R.id.remove_button)
        
        setupUI()
        setupListeners()
        setupFocusOrder()
    }
    
    private fun setupUI() {
        appNameText.text = "Установить лимит для: $appName"
        
        // Setup hour picker (0-23)
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 23
        hoursPicker.wrapSelectorWheel = false
        
        // Setup minute picker (0-55 with 5 min step)
        minutesPicker.minValue = 0
        minutesPicker.maxValue = 11
        val minuteValues = Array(12) { i -> (i * 5).toString() }
        minutesPicker.displayedValues = minuteValues
        minutesPicker.wrapSelectorWheel = false
        
        // Set current values
        if (currentLimitMinutes > 0) {
            val hours = (currentLimitMinutes / 60).toInt()
            val minutes = (currentLimitMinutes % 60).toInt()
            
            hoursPicker.value = hours
            
            minutesPicker.value = minutes / 5
            
            removeButton.visibility = View.VISIBLE
        } else {
            hoursPicker.value = 2 // Default 2 hours
            minutesPicker.value = 0 // Default 0 minutes
            removeButton.visibility = View.GONE
        }
    }
    
    private fun setupListeners() {
        saveButton.setOnClickListener {
            val hours = hoursPicker.value
            val minutes = minutesPicker.value * 5
            
            val totalMinutes = (hours * 60 + minutes).toLong()
            
            if (totalMinutes > 0) {
                onLimitSetListener?.invoke(packageName, totalMinutes)
                dismiss()
            }
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        removeButton.setOnClickListener {
            onLimitSetListener?.invoke(packageName, 0) // 0 means remove limit
            dismiss()
        }
    }
    
    fun setOnLimitSetListener(listener: (String, Long) -> Unit) {
        onLimitSetListener = listener
    }

    private fun setupFocusOrder() {
        if (removeButton.visibility == View.VISIBLE) {
            cancelButton.nextFocusRightId = R.id.remove_button
            removeButton.nextFocusLeftId = R.id.cancel_button
            removeButton.nextFocusRightId = R.id.save_button
            saveButton.nextFocusLeftId = R.id.remove_button
        } else {
            cancelButton.nextFocusRightId = R.id.save_button
            saveButton.nextFocusLeftId = R.id.cancel_button
        }
    }
    
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        saveButton.requestFocus()
    }
}