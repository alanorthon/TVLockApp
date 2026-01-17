package com.example.tvlockapp.ui.pin

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tvlockapp.R
import com.example.tvlockapp.data.preference.PinManager

class PinFragment : Fragment() {

    private lateinit var pinDisplay: TextView
    private lateinit var titleText: TextView
    private lateinit var instructionText: TextView
    private var currentPin = ""
    private var mode = "setup" // setup, verify, confirm
    private var firstPin = ""

    companion object {
        fun newInstance(mode: String): PinFragment {
            val fragment = PinFragment()
            val args = Bundle()
            args.putString("mode", mode)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mode = arguments?.getString("mode") ?: "setup"
        
        pinDisplay = view.findViewById(R.id.pin_display)
        titleText = view.findViewById(R.id.title_text)
        instructionText = view.findViewById(R.id.instruction_text)
        
        setupUI()
        setupNumberButtons(view)
        setupActionButtons(view)
    }
    
    private fun setupUI() {
        when (mode) {
            "setup" -> {
                titleText.text = "Установка PIN-кода"
                instructionText.text = "Введите 4-значный PIN-код"
            }
            "verify" -> {
                titleText.text = "Вход в приложение"
                instructionText.text = "Введите PIN-код"
            }
            "confirm" -> {
                titleText.text = "Подтверждение PIN-кода"
                instructionText.text = "Повторите PIN-код"
            }
        }
    }
    
    private fun setupNumberButtons(view: View) {
        val buttonIds = arrayOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        )
        
        buttonIds.forEachIndexed { index, buttonId ->
            view.findViewById<Button>(buttonId).setOnClickListener {
                addDigit(index.toString())
            }
        }
    }
    
    private fun setupActionButtons(view: View) {
        view.findViewById<Button>(R.id.btn_clear).setOnClickListener {
            clearPin()
        }
        
        view.findViewById<Button>(R.id.btn_delete).setOnClickListener {
            deleteLastDigit()
        }
    }
    
    private fun addDigit(digit: String) {
        if (currentPin.length < 4) {
            currentPin += digit
            updatePinDisplay()
            
            if (currentPin.length == 4) {
                handlePinComplete()
            }
        }
    }
    
    private fun deleteLastDigit() {
        if (currentPin.isNotEmpty()) {
            currentPin = currentPin.dropLast(1)
            updatePinDisplay()
        }
    }
    
    private fun clearPin() {
        currentPin = ""
        updatePinDisplay()
    }
    
    private fun updatePinDisplay() {
        pinDisplay.text = "*".repeat(currentPin.length) + "_".repeat(4 - currentPin.length)
    }
    
    private fun handlePinComplete() {
        when (mode) {
            "setup" -> {
                firstPin = currentPin
                mode = "confirm"
                currentPin = ""
                setupUI()
                updatePinDisplay()
            }
            "confirm" -> {
                if (currentPin == firstPin) {
                    PinManager.setPin(requireContext(), currentPin)
                    Toast.makeText(context, "PIN-код установлен", Toast.LENGTH_SHORT).show()
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "PIN-коды не совпадают", Toast.LENGTH_SHORT).show()
                    mode = "setup"
                    currentPin = ""
                    firstPin = ""
                    setupUI()
                    updatePinDisplay()
                }
            }
            "verify" -> {
                val savedPin = PinManager.getPin(requireContext())
                if (currentPin == savedPin) {
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    Toast.makeText(context, "Неверный PIN-код", Toast.LENGTH_SHORT).show()
                    currentPin = ""
                    updatePinDisplay()
                }
            }
        }
    }
}