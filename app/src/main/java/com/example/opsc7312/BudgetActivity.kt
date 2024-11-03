package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.Budget
import com.example.opsc7312.api.CategoriesResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BudgetActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var spnAccounts: Spinner
    private lateinit var lvCategories: ListView
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var accountNames: List<String> = emptyList()
    private var budgetCategories: List<Budget> = emptyList()
    private var selectedAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_page)

        // Initialize UI components
        initViews()
        setSpinnerListener()
        fetchUserAccounts()
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        spnAccounts = findViewById(R.id.spnAccounts)
        lvCategories = findViewById(R.id.lvCategories)
        btnAdd = findViewById(R.id.btnAdd)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, BudgetCreateActivity::class.java))
        }

        btnEdit.setOnClickListener {
            startActivity(Intent(this, BudgetEditActivity::class.java))
        }

        btnDelete.setOnClickListener {
            startActivity(Intent(this, BudgetDeleteActivity::class.java))
        }
    }

    private fun fetchUserAccounts() {
        val userId = getUserIdFromPreferences()
        if (userId != null) {
            val call = RetrofitClient.apiService.getUserAccounts(userId)
            call.enqueue(object : Callback<AccountsResponse> {
                override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        accountNames = response.body()!!.accounts.map { it.name }
                        setupAccountSpinner()
                    } else {
                        showToast("Failed to fetch accounts")
                    }
                }

                override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                    showToast("Failed to connect: ${t.message}")
                }
            })
        } else {
            showToast("User session is missing. Please log in again.")
        }
    }

    private fun setupAccountSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnAccounts.adapter = adapter
    }

    private fun setSpinnerListener() {
        spnAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames[position]
                fetchBudgetCategories(selectedAccount!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }
    }

    private fun fetchBudgetCategories(accountName: String) {
        val userId = getUserIdFromPreferences()
        if (userId != null) {
            val call = RetrofitClient.apiService.getAccountCategories(userId, accountName)
            call.enqueue(object : Callback<CategoriesResponse> {
                override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        budgetCategories = response.body()!!.budgets
                        displayCategories()
                    } else {
                        showToast("Failed to fetch categories")
                    }
                }

                override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                    showToast("Failed to connect: ${t.message}")
                }
            })
        } else {
            showToast("User session is missing. Please log in again.")
        }
    }

    private fun displayCategories() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, budgetCategories.map { "${it.category}: Budgeted - ${it.amountBudgeted}, Spent - ${it.amountSpent}" })
        lvCategories.adapter = adapter
    }

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
