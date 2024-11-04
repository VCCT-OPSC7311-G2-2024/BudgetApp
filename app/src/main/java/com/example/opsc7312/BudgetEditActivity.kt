package com.example.opsc7312

<<<<<<< HEAD
=======
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.UpdateBudgetAmountResponse
import com.example.opsc7312.api.UpdateSpentAmountResponse
import com.example.opsc7312.utils.NotificationHelper
>>>>>>> 484ea648b22cdb69fbc4c997aee5d145eec2f062
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.UpdateBudgetAmountResponse
import com.example.opsc7312.api.UpdateSpentAmountResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.opsc7312.utils.NotificationHelper

class BudgetEditActivity : ComponentActivity() {

    private lateinit var btnEditBudget: Button
    private lateinit var spinnerAccounts: Spinner
    private lateinit var txtCategoryName: EditText
    private lateinit var txtAmountBudgeted: EditText
    private lateinit var txtAmountSpent: EditText
    private lateinit var btnHome: Button
    private lateinit var budgetDao: BudgetDao // Declare budgetDao

    private var accountNames: List<String> = emptyList()
    private var selectedAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgetedit_page)

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
<<<<<<< HEAD
            showToast(getString(R.string.please_fill_in_all_fields_correctly))
=======
            showToast("Please fill in all fields correctly")
>>>>>>> 484ea648b22cdb69fbc4c997aee5d145eec2f062
        } else {
            // Check if spending exceeds budget and show notification
            if (amountSpent > amountBudgeted) {
                NotificationHelper.showNotification(
                    context = this,
                    title = "Budget Alert",
                    message = "You have exceeded your budget for $category"
                )
            }

            // Continue with updating the budget
            if (userId != null) {
                updateBudget(userId, selectedAccount!!, category, amountBudgeted, amountSpent)
            }
<<<<<<< HEAD
        }
    }
    private fun fetchUserAccounts(userId: String) {
        if (isNetworkAvailable()) {
            val call = RetrofitClient.apiService.getUserAccounts(userId)
            call.enqueue(object : Callback<AccountsResponse> {
                override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        accountNames = response.body()!!.accounts.map { it.name }
                        saveAccountsToPreferences(accountNames) // Save to preferences
                        setupAccountSpinnerAdapter()
                    } else {
                        loadAccountsFromPreferences() // Load from preferences if API fails
                    }
                }

                override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                    loadAccountsFromPreferences() // Load from preferences on failure
                }
            })
        } else {
            loadAccountsFromPreferences() // Load from preferences if no network
=======
>>>>>>> 484ea648b22cdb69fbc4c997aee5d145eec2f062
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
        setupAccountSpinnerAdapter()
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
                    showToast(getString(R.string.failed_to_update_budget_amount))
                    // Save to Room for offline
                    saveBudgetActionToRoom(userId, accountName, category, amountBudgeted, "edit", amountSpent)
                }
            }

            override fun onFailure(call: Call<UpdateBudgetAmountResponse>, t: Throwable) {
                showToast(getString(R.string.failed_to_connect, t.message))
                // Save to Room for offline
                saveBudgetActionToRoom(userId, accountName, category, amountBudgeted, "edit", amountSpent)
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
                    showToast(getString(R.string.failed_to_update_spent_amount))
                }
            }

            override fun onFailure(call: Call<UpdateSpentAmountResponse>, t: Throwable) {
                showToast(getString(R.string.failed_to_connect, t.message))
            }
        })
    }

    private fun saveBudgetActionToRoom(userId: String, accountName: String, category: String, amountBudgeted: Double, actionType: String, amountSpent: Double) {
        val budgetEntity = BudgetEntity(
            userId = userId,
            accountName = accountName,
            category = category,
            amountBudgeted = amountBudgeted,
            amountSpent = amountSpent,
            actionType = actionType,
            isSynced = false
        )
        // Use a coroutine to save to Room
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
}
