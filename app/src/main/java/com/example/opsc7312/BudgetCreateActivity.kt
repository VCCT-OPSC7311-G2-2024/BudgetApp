package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.AddCategoryRequest
import com.example.opsc7312.api.AddCategoryResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BudgetCreateActivity : ComponentActivity() {

    private lateinit var txtCategory: EditText
    private lateinit var txtAmountBudgeted: EditText
    private lateinit var btnBudgetCreate: Button
    private lateinit var spinnerAccounts: Spinner
    private lateinit var btnHome: Button

    private var accountNames: List<String> = emptyList() // To store account names
    private var selectedAccount: String? = null // To store selected account name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgetcreate_page)

        // Initialize UI elements
        initViews()

        // Retrieve userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            fetchUserAccounts(userId)
        } else {
            showToast("User session is missing. Please log in again.")
        }

        // Set up spinner selection listener
        setupSpinnerListener()

        // Set button listeners
        setupButtonListeners(userId)
    }

    private fun initViews() {
        txtCategory = findViewById(R.id.txtCategory)
        txtAmountBudgeted = findViewById(R.id.txtAmountBudgeted)
        btnBudgetCreate = findViewById(R.id.btnBudgetCreate)
        spinnerAccounts = findViewById(R.id.spinnerAccounts)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun setupSpinnerListener() {
        spinnerAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames[position] // Store the selected account
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }
    }

    private fun setupButtonListeners(userId: String?) {
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnBudgetCreate.setOnClickListener {
            val category = txtCategory.text.toString().trim()
            val amountBudgeted = txtAmountBudgeted.text.toString().trim().toDoubleOrNull()

            if (category.isEmpty() || amountBudgeted == null || amountBudgeted <= 0 || selectedAccount == null) {
                showToast("Please fill in all fields correctly")
            } else {
                if (userId != null && selectedAccount != null) {
                    addCategoryToAccount(userId, selectedAccount!!, category, amountBudgeted)
                }
            }
        }
    }

    private fun fetchUserAccounts(userId: String) {
        val call = RetrofitClient.apiService.getUserAccounts(userId)
        call.enqueue(object : Callback<AccountsResponse> {
            override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val accountsResponse = response.body()!!
                    accountNames = accountsResponse.accounts.map { it.name } // Extract account names

                    // Populate the spinner with account names
                    val adapter = ArrayAdapter(this@BudgetCreateActivity, android.R.layout.simple_spinner_item, accountNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerAccounts.adapter = adapter
                } else {
                    showToast("Failed to fetch accounts")
                }
            }

            override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun addCategoryToAccount(userId: String, accountName: String, category: String, amountBudgeted: Double) {
        val request = AddCategoryRequest(category = category, amountBudgeted = amountBudgeted, amountSpent = 0.0)

        val call = RetrofitClient.apiService.addCategory(userId, accountName, request)
        call.enqueue(object : Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    showToast("Budget created successfully!")
                } else {
                    Log.e("BudgetCreateActivity", "Error: ${response.errorBody()?.string()}")
                    showToast("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                Log.e("BudgetCreateActivity", "Failed to connect: ${t.message}")
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
