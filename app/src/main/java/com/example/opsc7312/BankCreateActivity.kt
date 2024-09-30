package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AddAccountRequest
import com.example.opsc7312.api.AddAccountResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BankCreateActivity : ComponentActivity() {

    private lateinit var txtBankName: EditText
    private lateinit var txtBankType: EditText
    private lateinit var txtBankDeposit: EditText
    private lateinit var btnBankCreate: Button
    private lateinit var btnHome: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bankcreate_page)

        // Initialize UI elements
        initViews()

        // Get SharedPreferences instance
        sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        btnHome.setOnClickListener {
            navigateToHome()
        }

        btnBankCreate.setOnClickListener {
            handleCreateAccount()
        }
    }

    private fun initViews() {
        txtBankName = findViewById(R.id.txtBankName)
        txtBankType = findViewById(R.id.txtBankType)
        txtBankDeposit = findViewById(R.id.txtBankDeposit)
        btnBankCreate = findViewById(R.id.btnBankCreate)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    private fun handleCreateAccount() {
        val bankName = txtBankName.text.toString().trim()
        val bankType = txtBankType.text.toString().trim()
        val bankDepositString = txtBankDeposit.text.toString().trim()

        val bankDeposit: Double? = try {
            if (bankDepositString.isNotEmpty()) {
                bankDepositString.toDouble()
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }

        if (validateInputs(bankName, bankType, bankDeposit)) {
            val userId = sharedPreferences.getString("userId", null)
            if (userId != null) {
                createAccount(userId, bankName, bankType, bankDeposit!!)
            } else {
                showToast("User ID not found. Please log in again.")
            }
        } else {
            showToast("Please fill in all fields correctly")
        }
    }

    private fun validateInputs(bankName: String, bankType: String, bankDeposit: Double?): Boolean {
        return bankName.isNotEmpty() && bankType.isNotEmpty() && bankDeposit != null && bankDeposit > 0
    }

    private fun createAccount(userId: String, accountName: String, accountType: String, amount: Double) {
        val request = AddAccountRequest(
            name = accountName,
            type = accountType,
            amount = amount,
            budgets = emptyList() // Initial empty list for budgets
        )

        // Call the API to create the account
        RetrofitClient.apiService.addAccount(userId, request).enqueue(object : Callback<AddAccountResponse> {
            override fun onResponse(call: Call<AddAccountResponse>, response: Response<AddAccountResponse>) {
                if (response.isSuccessful) {
                    showToast("Account created successfully!")
                    navigateToBankActivity(accountName, userId)
                } else {
                    showToast("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddAccountResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun navigateToBankActivity(accountName: String, userId: String) {
        val intent = Intent(this, BankActivity::class.java).apply {
            putExtra("accountName", accountName)
            putExtra("userId", userId)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
