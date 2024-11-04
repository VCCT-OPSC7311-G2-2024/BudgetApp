package com.example.opsc7312

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.FragmentActivity
import com.example.opsc7312.security.BiometricManager
import java.util.Locale

class SettingsActivity : FragmentActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnLang: Button
    private lateinit var lblUsername: TextView
    private lateinit var lblEmail: TextView
    private lateinit var switchBiometric: Switch
    private lateinit var biometricManager: BiometricManager
    private lateinit var tvBiometricStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        loadAppLanguage()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        initializeViews()
        setupBiometricAuthentication()
        displayUsername()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnHome = findViewById(R.id.btnHome)
        btnLang = findViewById(R.id.btnLang)
        lblUsername = findViewById(R.id.lblUsername)
        lblEmail = findViewById(R.id.lblEmail)
        switchBiometric = findViewById(R.id.switchBiometric)
        tvBiometricStatus = findViewById(R.id.tvBiometricStatus)
    }

    private fun setupClickListeners() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnLang.setOnClickListener {
            showLanguagePickerDialog()
        }
    }

    private fun loadAppLanguage() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("selected_language", "en") // Default to "en"
        if (languageCode != null) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)

            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }


    private fun showLanguagePickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_language_picker)
        dialog.setTitle("@string/select_language")

        val languageListView = dialog.findViewById<ListView>(R.id.languageListView)
        val languages = arrayOf("English", "Afrikaans", "Zulu")
        val languageCodes = arrayOf("en", "af", "zu")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, languages)
        languageListView.adapter = adapter

        languageListView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languages[position]
            val selectedLanguageCode = languageCodes[position]
            changeAppLanguage(selectedLanguageCode)
            showToast(getString(R.string.selected_language, selectedLanguage))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun changeAppLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)


        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_language", languageCode)
            apply()
        }

        recreate()
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
                showToast(getString(R.string.biometric_authentication_enabled))
            },
            onError = { errorMessage ->
                switchBiometric.isChecked = false
                updateBiometricStatus(false)
                showToast(getString(R.string.authentication_error, errorMessage))
            },
            onFailed = {
                switchBiometric.isChecked = false
                updateBiometricStatus(false)
                showToast(getString(R.string.authentication_failed))
            }
        )
    }

    private fun disableBiometric() {
        biometricManager.setBiometricEnabled(false)
        updateBiometricStatus(false)
        showToast(getString(R.string.biometric_authentication_disabled))
    }

    private fun updateBiometricStatus(enabled: Boolean) {
        tvBiometricStatus.text = if (enabled) {
            getString(R.string.biometric_authentication_is_enabled)
        } else {
            getString(R.string.biometric_authentication_is_disabled)
        }
    }

    private fun displayUsername() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Unknown User")
        lblUsername.text = username
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
