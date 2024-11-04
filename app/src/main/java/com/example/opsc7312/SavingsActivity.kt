package com.example.opsc7312

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import kotlin.math.pow

class SavingsActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var spnAccountType: Spinner
    private lateinit var txtPrincipal: EditText
    private lateinit var txtInterestRate: EditText
    private lateinit var txtTimePeriod: EditText
    private lateinit var btnCalculate: Button
    private lateinit var txtResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savings_page)

        initializeViews()
        setupSpinner()
        setupListeners()
    }

    private fun initializeViews() {
        btnHome = findViewById(R.id.btnHome)
        spnAccountType = findViewById(R.id.spnAccountType)
        txtPrincipal = findViewById(R.id.txtPrincipal)
        txtInterestRate = findViewById(R.id.txtInterestRate)
        txtTimePeriod = findViewById(R.id.txtTimePeriod)
        btnCalculate = findViewById(R.id.btnCalculate)
        txtResult = findViewById(R.id.txtResult)
    }

    private fun setupSpinner() {
        val accountTypes = arrayOf(
            "Simple Interest",
            "Compound Interest (Annual)",
            "Compound Interest (Semi-Annual)",
            "Compound Interest (Quarterly)",
            "Compound Interest (Monthly)"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnAccountType.adapter = adapter
    }

    private fun setupListeners() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnCalculate.setOnClickListener {
            calculateSavings()
        }
    }

    private fun calculateSavings() {
        try {
            val principal = txtPrincipal.text.toString().toDouble()
            val rate = txtInterestRate.text.toString().toDouble() / 100
            val time = txtTimePeriod.text.toString().toDouble()

            val result = when (spnAccountType.selectedItemPosition) {
                0 -> calculateSimpleInterest(principal, rate, time)
                1 -> calculateCompoundInterest(principal, rate, time, 1)
                2 -> calculateCompoundInterest(principal, rate, time, 2)
                3 -> calculateCompoundInterest(principal, rate, time, 4)
                4 -> calculateCompoundInterest(principal, rate, time, 12)
                else -> 0.0
            }

            displayResult(result)
        } catch (e: Exception) {
            showToast("Please enter valid numbers")
        }
    }

    private fun calculateSimpleInterest(principal: Double, rate: Double, time: Double): Double {
        return principal * (1 + rate * time)
    }

    private fun calculateCompoundInterest(
        principal: Double,
        rate: Double,
        time: Double,
        compoundingFrequency: Int
    ): Double {
        return principal * (1 + rate / compoundingFrequency).pow(compoundingFrequency * time)
    }

    private fun displayResult(amount: Double) {
        txtResult.text = String.format("Final Amount: R%.2f", amount)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}