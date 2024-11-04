package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.CategoriesResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetDeleteActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnDeleteBudget: Button
    private lateinit var spinnerAccounts: Spinner
    private lateinit var spinnerCategories: Spinner
    private lateinit var noCategoriesTextView: TextView
    private lateinit var budgetDao: BudgetDao // Declare budgetDao

    private var accountNames: List<String> = emptyList()
    private var categoryNames: List<String> = emptyList()

    private var selectedAccount: String? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budgetdelete_page)

        // Initialize UI elements
        initViews()

        // Initialize budgetDao
        budgetDao = AppDatabase.getDatabase(applicationContext).budgetDao()

        // Set up listeners
        setupListeners()

        // Retrieve userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        if (userId != null) {
            fetchUserAccounts(userId)
        } else {
            showToast(getString(R.string.user_session_is_missing_please_log_in_again))
        }
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget)
        spinnerAccounts = findViewById(R.id.spinnerAccounts)
        spinnerCategories = findViewById(R.id.spinnerCategories)
        noCategoriesTextView = findViewById(R.id.noCategoriesTextView)
    }

    private fun setupListeners() {
        // Go back to home
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        // Handle account selection
        spinnerAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames[position]
                // Fetch categories for the selected account
                fetchAccountCategories(selectedAccount!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }

        // Handle category selection
        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = categoryNames[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }

        // Handle delete button click
        btnDeleteBudget.setOnClickListener {
            if (selectedAccount != null && selectedCategory != null) {
                showDeleteConfirmationDialog(selectedAccount!!, selectedCategory!!)
            } else {
                showToast(getString(R.string.please_select_an_account_and_category))
            }
        }
    }

    private fun showDeleteConfirmationDialog(accountName: String, category: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.delete_category))
        builder.setMessage(
            getString(
                R.string.are_you_sure_you_want_to_delete_the_category_from_account,
                category,
                accountName
            ))

        // Set up the confirmation buttons
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            deleteCategory(accountName, category)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }

        val dialog: android.app.AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteCategory(accountName: String, category: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: return

        if (isNetworkAvailable()) {
            val call = RetrofitClient.apiService.deleteCategory(userId, accountName, category)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        showToast(getString(R.string.category_deleted_successfully, category))
                        fetchAccountCategories(accountName) // Refresh categories
                    } else {
                        showToast(
                            getString(
                                R.string.failed_to_delete_category,
                                response.errorBody()?.string()
                            ))
                        saveDeleteActionOffline(userId, accountName, category)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast(getString(R.string.failed_to_connect, t.message))
                    saveDeleteActionOffline(userId, accountName, category)
                }
            })
        } else {
            saveDeleteActionOffline(userId, accountName, category)
            showToast(getString(R.string.no_internet_connection))
        }
    }

    private fun saveDeleteActionOffline(userId: String, accountName: String, category: String) {
        val budgetEntity = BudgetEntity(
            userId = userId,
            accountName = accountName,
            category = category,
            amountBudgeted = 0.0,
            amountSpent = 0.0,
            actionType = "delete",
            isSynced = false
        )
        CoroutineScope(Dispatchers.IO).launch {
            budgetDao.insertBudgetAction(budgetEntity)
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
                        populateSpinner()
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
        populateSpinner()
    }

    private fun fetchAccountCategories(accountName: String) {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null) ?: return

        if (isNetworkAvailable()) {
            val call = RetrofitClient.apiService.getAccountCategories(userId, accountName)
            call.enqueue(object : Callback<CategoriesResponse> {
                override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                    if (response.isSuccessful) {
                        val categoriesResponse = response.body()
                        if (categoriesResponse != null && categoriesResponse.budgets.isNotEmpty()) {
                            noCategoriesTextView.visibility = TextView.GONE
                            categoryNames = categoriesResponse.budgets.map { it.category }
                            saveCategoriesToPreferences(accountName, categoryNames)
                        } else {
                            categoryNames = emptyList()
                            noCategoriesTextView.text = "No categories found for this account."
                            noCategoriesTextView.visibility = TextView.VISIBLE
                        }
                        populateCategorySpinner()
                        btnDeleteBudget.isEnabled = categoryNames.isNotEmpty()
                    } else {
                        showToast(getString(R.string.failed_to_fetch_categories))
                        loadCategoriesFromPreferences(accountName)
                    }
                }

                override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                    showToast(getString(R.string.failed_to_connect, t.message))
                    loadCategoriesFromPreferences(accountName)
                }
            })
        } else {
            loadCategoriesFromPreferences(accountName)
        }
    }

    private fun saveCategoriesToPreferences(accountName: String, categories: List<String>) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("categories_$accountName", categories.toSet())
        editor.apply()
    }

    private fun loadCategoriesFromPreferences(accountName: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val categoriesSet = sharedPreferences.getStringSet("categories_$accountName", emptySet())
        categoryNames = categoriesSet?.toList() ?: emptyList()

        if (categoryNames.isEmpty()) {
            noCategoriesTextView.text = "No categories found for this account."
            noCategoriesTextView.visibility = TextView.VISIBLE
            btnDeleteBudget.isEnabled = false
        } else {
            noCategoriesTextView.visibility = TextView.GONE
            btnDeleteBudget.isEnabled = true
        }
        populateCategorySpinner()
    }

    private fun populateCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategories.adapter = adapter
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

    private fun populateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAccounts.adapter = adapter
    }
}
