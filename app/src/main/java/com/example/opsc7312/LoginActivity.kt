package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.opsc7312.api.LoginRequest
import com.example.opsc7312.api.LoginResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import com.example.opsc7312.security.BiometricManager

class LoginActivity : FragmentActivity() {

    private lateinit var btnLogin: Button
    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        // Initialize BiometricManager
        biometricManager = BiometricManager(this)

        // Initialize UI components
        initViews()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Check if biometric authentication is available and enabled
        if (biometricManager.isBiometricAvailable() && biometricManager.isBiometricEnabled()) {
            showBiometricLogin()
        }

        // Set login button click listener
        btnLogin.setOnClickListener { handleLoginClick() }
    }

    private fun initViews() {
        btnLogin = findViewById(R.id.btnLogin)
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassword)
    }

    private fun showBiometricLogin() {
        biometricManager.showBiometricPrompt(
            this,
            onSuccess = {
                val storedUsername = sharedPreferences.getString("stored_username", null)
                val storedPassword = sharedPreferences.getString("stored_password", null)

                if (storedUsername != null && storedPassword != null) {
                    // Use the stored credentials to login
                    loginUser(storedUsername, storedPassword)
                } else {
                    showToast(getString(R.string.stored_credentials_not_found))
                }
            },
            onError = { errorMessage ->
                showToast(getString(R.string.authentication_error, errorMessage))
            },
            onFailed = {
                showToast(getString(R.string.authentication_failed))
            }
        )
    }

    private fun handleLoginClick() {
        val username = txtUsername.text.toString().trim()
        val password = txtPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
        } else {
            val hashedPassword = hashPassword(password)
            loginUser(username, hashedPassword)
        }
    }

    private fun loginUser(username: String, password: String) {
        val request = LoginRequest(username, password)
        val call = RetrofitClient.apiService.loginUser(request)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "API Response: $response")

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Response Body: $loginResponse")

                    if (loginResponse != null && loginResponse.message == "Login successful") {
                        // Store credentials for biometric login
                        if (biometricManager.isBiometricAvailable()) {
                            sharedPreferences.edit().apply {
                                putString("stored_username", username)
                                putString("stored_password", password)
                                apply()
                            }
                        }

                        // Save user session
                        saveUserSession(loginResponse.userId, username, loginResponse.email)

                        showToast(getString(R.string.login_successful))
                        navigateToHome(loginResponse.email)
                    } else {
                        showToast(getString(R.string.login_failed, loginResponse?.message))
                    }
                } else {
                    showToast(getString(R.string.error, response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Failed to connect: ${t.message}")
                showToast(getString(R.string.failed_to_connect, t.message))
            }
        })
    }

    private fun saveUserSession(userId: String, username: String, email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.apply()
    }

    private fun navigateToHome(email: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user_email", email)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
