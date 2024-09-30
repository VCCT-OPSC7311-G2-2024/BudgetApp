package com.example.opsc7312

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class SavingsActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnMainCreateSavings: Button
    private lateinit var btnMainEditSavings: Button
    private lateinit var btnMainDeleteSavings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savings_page)

        // Initialize buttons
        initViews()

        // Set button click listeners
        setupButtonListeners()
    }

    // Initialize button views
    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnMainCreateSavings = findViewById(R.id.btnMainCreateSavings)
        btnMainEditSavings = findViewById(R.id.btnMainEditSavings)
        btnMainDeleteSavings = findViewById(R.id.btnMainDeleteSavings)
    }

    // Setup click listeners for buttons
    private fun setupButtonListeners() {
        btnHome.setOnClickListener {
            navigateToActivity(HomeActivity::class.java)
        }

        btnMainCreateSavings.setOnClickListener {
            navigateToActivity(SavingsCreateActivity::class.java)
        }

        btnMainEditSavings.setOnClickListener {
            navigateToActivity(SavingsEditActivity::class.java)
        }

        btnMainDeleteSavings.setOnClickListener {
            navigateToActivity(SavingsDeleteActivity::class.java)
        }
    }

    // Reusable function to navigate to another activity
    private fun <T> navigateToActivity(activity: Class<T>) {
        startActivity(Intent(this, activity))
    }
}
