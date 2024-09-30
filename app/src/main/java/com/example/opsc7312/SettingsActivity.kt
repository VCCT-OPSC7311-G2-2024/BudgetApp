package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var lblPersonalInfoUsername: TextView
    private lateinit var lblUsername: TextView
    private lateinit var lblEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        btnHome = findViewById(R.id.btnHome)
        lblPersonalInfoUsername = findViewById(R.id.lblPersonalInfoUsername)
        lblUsername = findViewById(R.id.lblUsername)
        lblEmail = findViewById(R.id.lblEmail)

        // Set click listener for the Home button
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Retrieve and display the logged-in username
        displayUsername()
    }

    // Function to retrieve username from SharedPreferences and display it
    private fun displayUsername() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Unknown User")

        // Display the username in the TextView
        lblPersonalInfoUsername.text = username
        lblUsername.text = username
    }
}
