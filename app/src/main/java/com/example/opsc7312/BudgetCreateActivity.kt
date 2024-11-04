package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetCreateActivity : ComponentActivity() {

    private lateinit var txtCategory: EditText
    private lateinit var txtAmountBudgeted: EditText
    private lateinit var btnBudgetCreate: Button
    private lateinit var spinnerAccounts: Spinner
    private lateinit var btnHome: Button
    private lateinit var budgetDao: BudgetDao // Declare budgetDao

    private var accountNames: List<String> = emptyList() // To store account names
    private var selectedAccount: String? = null // To store selected account name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgetcreate_page)

        // Initialize UI elements
        initViews()

        // Initialize budgetDao
        budgetDao = AppDatabase.getDatabase(applicationContext).budgetDao()

        // Retrieve userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            fetchUserAccounts(userId)
        } else {
            showToast(getString(R.string.user_session_is_missing_please_log_in_again))
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
                showToast(getString(R.string.please_fill_in_all_fields_correctly))
            } else {
                if (userId != null && selectedAccount != null) {
                    addCategoryToAccount(userId, selectedAccount!!, category, amountBudgeted)
                }
            }
        }
    }

    private fun fetchUserAccounts(userId: String) {
        if (isNetworkAvailable()) {
            val call = RetrofitClient.apiService.getUserAccounts(userId)
            call.enqueue(object : Callback<AccountsResponse> {
                override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        accountNames = response.body()!!.accounts.map { it.name }
                        saveAccountsToPreferences(accountNames)
                        setupAccountSpinner()
                    } else {
                        loadAccountsFromPreferences()
                    }
                }

                override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                    loadAccountsFromPreferences()
                }
            })
        } else {
            loadAccountsFromPreferences()
        }
    }

    private fun saveAccountsToPreferences(accounts: List<String>) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("account_names", accounts.toSet())
        editor.apply()
    }

    private fun loadAccountsFromPreferences() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val accountsSet = sharedPreferences.getStringSet("account_names", emptySet())
        accountNames = accountsSet?.toList() ?: emptyList()
        setupAccountSpinner()
    }

    private fun addCategoryToAccount(userId: String, accountName: String, category: String, amountBudgeted: Double) {
        val request = AddCategoryRequest(category = category, amountBudgeted = amountBudgeted, amountSpent = 0.0)

        val call = RetrofitClient.apiService.addCategory(userId, accountName, request)
        call.enqueue(object : Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    showToast(getString(R.string.budget_created_successfully))
                } else {
                    showToast(getString(R.string.error, response.errorBody()?.string()))
                    // Save to Room for offline
                    saveBudgetActionToRoom(userId, accountName, category, amountBudgeted, "create")
                    
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                showToast(getString(R.string.failed_to_connect, t.message))
                // Save to Room for offline
                saveBudgetActionToRoom(userId, accountName, category, amountBudgeted, "create")
            }
        })
    }

    private fun saveBudgetActionToRoom(userId: String, accountName: String, category: String, amountBudgeted: Double, actionType: String) {
        val budgetEntity = BudgetEntity(
            userId = userId,
            accountName = accountName,
            category = category,
            amountBudgeted = amountBudgeted,
            amountSpent = 0.0,
            actionType = actionType,
            isSynced = false
        )
        CoroutineScope(Dispatchers.IO).launch {
            budgetDao.insertBudgetAction(budgetEntity)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupAccountSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAccounts.adapter = adapter
    }
}
