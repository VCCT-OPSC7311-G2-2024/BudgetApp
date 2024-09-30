package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.Category
import com.example.opsc7312.api.CategoriesResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BudgetActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnMainBudgetCreate: Button
    private lateinit var btnMainEditBudget: Button
    private lateinit var btnMainDeleteBudget: Button
    private lateinit var spnBudgetCategory: Spinner

    private var budgetCategories: List<Category> = emptyList() // To store budget categories
    private var selectedCategory: String? = null // To store selected budget category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_page)

        // Initialize UI elements
        initViews()

        // Fetch budget categories
        val userId = getUserIdFromPreferences() // Assuming user ID is required for fetching categories
        if (userId != null) {
            fetchBudgetCategories(userId) // Fetch budget categories
        } else {
            showToast("User session is missing. Please log in again.")
        }

        // Set spinner selection listener
        setSpinnerListener()

        // Set button click listeners
        setButtonListeners()
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnMainBudgetCreate = findViewById(R.id.btnMainBudgetCreate)
        btnMainEditBudget = findViewById(R.id.btnMainEditBudget)
        btnMainDeleteBudget = findViewById(R.id.btnMainDeleteBudget)
        spnBudgetCategory = findViewById(R.id.spnBudgetCategory)
    }

    private fun getUserIdFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setSpinnerListener() {
        spnBudgetCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = budgetCategories[position].name // Store the selected category name
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }
    }

    private fun setButtonListeners() {
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnMainBudgetCreate.setOnClickListener {
            startActivity(Intent(this, BudgetCreateActivity::class.java))
        }

        btnMainEditBudget.setOnClickListener {
            startActivity(Intent(this, BudgetEditActivity::class.java))
        }

        btnMainDeleteBudget.setOnClickListener {
            startActivity(Intent(this, BudgetDeleteActivity::class.java))
        }
    }

    private fun fetchBudgetCategories(userId: String) {
        val accountName = "ActualAccountName" // Replace this with the actual account name
        val call = RetrofitClient.apiService.getAccountCategories(userId, accountName)


        call.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val categoriesResponse = response.body()!!
                    budgetCategories = categoriesResponse.categories // Get list of budget categories

                    // Populate the spinner with budget categories
                    val adapter = ArrayAdapter(
                        this@BudgetActivity,
                        android.R.layout.simple_spinner_item,
                        budgetCategories.map { it.name } // Use category names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spnBudgetCategory.adapter = adapter
                } else {
                    Log.e("BudgetActivity", "Response code: ${response.code()}, Message: ${response.message()}")
                    showToast("Failed to fetch budget categories: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.e("BudgetActivity", "Error fetching categories: ${t.message}")
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
        Log.e("BudgetActivity", "Error fetching categories: ${t.message}")
        showToast("Failed to connect: ${t.message}")
    }


}
