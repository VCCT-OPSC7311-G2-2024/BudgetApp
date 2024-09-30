package com.example.opsc7312

import com.example.opsc7312.api.RegisterRequest
import com.example.opsc7312.api.RegisterResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log for debugging
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class SignUpActivity : ComponentActivity() {

    private lateinit var btnSignUp: Button
    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)

        btnSignUp = findViewById(R.id.btnSignUp)
        txtUsername = findViewById(R.id.txtUsername)
        txtPassword = findViewById(R.id.txtPassword)
        txtEmail = findViewById(R.id.txtEmail)

        btnSignUp.setOnClickListener {
            val username = txtUsername.text.toString().trim()
            val password = txtPassword.text.toString().trim()
            val email = txtEmail.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val hashedPassword = hashPassword(password)
                registerUser(username, hashedPassword, email)
            }
        }
    }

    private fun registerUser(username: String, password: String, email: String) {
        val request = RegisterRequest(username, password, email)
        val call = RetrofitClient.apiService.registerUser(request)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    Log.d("SignUpActivity", "Response: ${registerResponse?.message}") // Log the message

                    if (registerResponse != null && registerResponse.message.contains("User registered successfully", ignoreCase = true)) {
                        Toast.makeText(this@SignUpActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        navigateToLogin() // Navigate to login on success
                    } else {
                        Toast.makeText(this@SignUpActivity, "Registration failed: ${registerResponse?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Failed to connect: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Navigate to the login activity
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish() // Optionally finish this activity to remove it from the back stack
    }
}
