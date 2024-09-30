package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.LoginRequest
import com.example.opsc7312.api.LoginResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class LoginActivity : ComponentActivity() {

    private lateinit var btnLogin: Button
    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        // Initialize UI components
        initViews()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Set login button click listener
        btnLogin.setOnClickListener { handleLoginClick() }
    }

    // Initialize UI components
    private fun initViews() {
        btnLogin = findViewById(R.id.btnLogin)
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassword)
    }

    // Handle the login button click
    private fun handleLoginClick() {
        val username = txtUsername.text.toString().trim()
        val password = txtPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
        } else {
            // Hash the password before sending to API
            val hashedPassword = hashPassword(password)
            loginUser(username, hashedPassword)
        }
    }

    // Function to log in the user
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
                        // Save userId, username, and email to SharedPreferences
                        saveUserSession(loginResponse.userId, username, loginResponse.email)

                        // Navigate to HomeActivity
                        showToast("Login successful!")
                        navigateToHome(loginResponse.email)
                    } else {
                        showToast("Login failed: ${loginResponse?.message}")
                    }
                } else {
                    showToast("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Failed to connect: ${t.message}")
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    // Save the userId, username, and email to SharedPreferences
    private fun saveUserSession(userId: String, username: String, email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email) // Save the email
        editor.apply()
    }

    // Navigate to HomeActivity
    private fun navigateToHome(email: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user_email", email) // Pass the email to HomeActivity
        startActivity(intent)
        finish() // Optionally finish this activity to remove it from the back stack
    }


    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Hash the password using SHA-256
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
