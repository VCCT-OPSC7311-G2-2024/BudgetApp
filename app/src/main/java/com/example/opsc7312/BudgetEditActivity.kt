package com.example.opsc7312

import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.UpdateBudgetAmountResponse
import com.example.opsc7312.api.UpdateSpentAmountResponse
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BudgetEditActivity : ComponentActivity() {

    private lateinit var btnEditBudget: Button
    private lateinit var spinnerAccounts: Spinner
    private lateinit var txtCategoryName: EditText
    private lateinit var txtAmountBudgeted: EditText
    private lateinit var txtAmountSpent: EditText
    private lateinit var btnHome: Button

    private var accountNames: List<String> = emptyList()
    private var selectedAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgetedit_page)

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
        setupAccountSpinnerListener()

        // Edit budget button listener
        btnEditBudget.setOnClickListener {
            handleEditBudgetClick(userId)
        }

        // Home button listener
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun initViews() {
        spinnerAccounts = findViewById(R.id.spinnerAccounts)
        txtCategoryName = findViewById(R.id.txtCategoryName)
        txtAmountBudgeted = findViewById(R.id.txtAmountBudgeted)
        txtAmountSpent = findViewById(R.id.txtAmountSpent)
        btnEditBudget = findViewById(R.id.btnEditBudget)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun setupAccountSpinnerListener() {
        spinnerAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }
    }

    private fun handleEditBudgetClick(userId: String?) {
        val category = txtCategoryName.text.toString().trim()
        val amountBudgeted = txtAmountBudgeted.text.toString().trim().toDoubleOrNull()
        val amountSpent = txtAmountSpent.text.toString().trim().toDoubleOrNull()

        if (category.isEmpty() || selectedAccount == null || amountBudgeted == null || amountSpent == null) {
            showToast("Please fill in all fields correctly")
        } else if (userId != null) {
            updateBudget(userId, selectedAccount!!, category, amountBudgeted, amountSpent)
        }
    }

    private fun fetchUserAccounts(userId: String) {
        val call = RetrofitClient.apiService.getUserAccounts(userId)
        call.enqueue(object : Callback<AccountsResponse> {
            override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    accountNames = response.body()!!.accounts.map { it.name }
                    setupAccountSpinnerAdapter()
                } else {
                    showToast("Failed to fetch accounts")
                }
            }

            override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun setupAccountSpinnerAdapter() {
        val adapter = ArrayAdapter(
            this@BudgetEditActivity,
            android.R.layout.simple_spinner_item,
            accountNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAccounts.adapter = adapter
    }

    private fun updateBudget(userId: String, accountName: String, category: String, amountBudgeted: Double, amountSpent: Double) {
        val updateBudgetAmountCall = RetrofitClient.apiService.editBudgetAmount(userId, accountName, category, amountBudgeted)
        updateBudgetAmountCall.enqueue(object : Callback<UpdateBudgetAmountResponse> {
            override fun onResponse(call: Call<UpdateBudgetAmountResponse>, response: Response<UpdateBudgetAmountResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()!!.message)
                    updateSpentAmount(userId, accountName, category, amountSpent)
                } else {
                    showToast("Failed to update budget amount")
                }
            }

            override fun onFailure(call: Call<UpdateBudgetAmountResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun updateSpentAmount(userId: String, accountName: String, category: String, amountSpent: Double) {
        val updateSpentAmountCall = RetrofitClient.apiService.editSpentAmount(userId, accountName, category, amountSpent)
        updateSpentAmountCall.enqueue(object : Callback<UpdateSpentAmountResponse> {
            override fun onResponse(call: Call<UpdateSpentAmountResponse>, response: Response<UpdateSpentAmountResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()!!.message)
                } else {
                    showToast("Failed to update spent amount")
                }
            }

            override fun onFailure(call: Call<UpdateSpentAmountResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
