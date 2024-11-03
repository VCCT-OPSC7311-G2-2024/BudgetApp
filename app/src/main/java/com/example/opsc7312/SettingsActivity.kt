package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentActivity
import com.example.opsc7312.security.BiometricManager

class SettingsActivity : FragmentActivity() {

    private lateinit var btnHome: Button
    private lateinit var lblPersonalInfoUsername: TextView
    private lateinit var lblUsername: TextView
    private lateinit var lblEmail: TextView
    private lateinit var switchBiometric: Switch
    private lateinit var biometricManager: BiometricManager
    private lateinit var tvBiometricStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        initializeViews()
        setupBiometricAuthentication()
        displayUsername()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnHome = findViewById(R.id.btnHome)
        lblPersonalInfoUsername = findViewById(R.id.lblPersonalInfoUsername)
        lblUsername = findViewById(R.id.lblUsername)
        lblEmail = findViewById(R.id.lblEmail)
        switchBiometric = findViewById(R.id.switchBiometric)
        tvBiometricStatus = findViewById(R.id.tvBiometricStatus)
    }

    private fun setupBiometricAuthentication() {
        biometricManager = BiometricManager(this)

        val statusMessage = biometricManager.checkBiometricStatus()
        Log.d("SettingsActivity", "Biometric status: $statusMessage")

        if (biometricManager.isBiometricAvailable()) {
            switchBiometric.visibility = View.VISIBLE
            tvBiometricStatus.visibility = View.VISIBLE
            switchBiometric.isChecked = biometricManager.isBiometricEnabled()
            updateBiometricStatus(biometricManager.isBiometricEnabled())

            switchBiometric.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    enableBiometric()
                } else {
                    disableBiometric()
                }
            }
        } else {
            switchBiometric.visibility = View.GONE
            tvBiometricStatus.visibility = View.VISIBLE
            tvBiometricStatus.text = statusMessage
        }
    }

    private fun enableBiometric() {
        biometricManager.showBiometricPrompt(
            this,
            onSuccess = {
                biometricManager.setBiometricEnabled(true)
                updateBiometricStatus(true)
                showToast("Biometric authentication enabled")
            },
            onError = { errorMessage ->
                switchBiometric.isChecked = false
                updateBiometricStatus(false)
                showToast("Authentication error: $errorMessage")
            },
            onFailed = {
                switchBiometric.isChecked = false
                updateBiometricStatus(false)
                showToast("Authentication failed")
            }
        )
    }

    private fun disableBiometric() {
        biometricManager.setBiometricEnabled(false)
        updateBiometricStatus(false)
        showToast("Biometric authentication disabled")
    }

    private fun updateBiometricStatus(enabled: Boolean) {
        tvBiometricStatus.text = if (enabled) {
            "Biometric authentication is enabled"
        } else {
            "Biometric authentication is disabled"
        }
    }

    private fun setupClickListeners() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun displayUsername() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Unknown User")
        lblPersonalInfoUsername.text = username
        lblUsername.text = username
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
