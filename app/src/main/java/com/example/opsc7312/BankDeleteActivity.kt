package com.example.opsc7312

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.DeleteAccountResponse
import com.example.opsc7312.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BankDeleteActivity : ComponentActivity() {

    private lateinit var spinnerAccounts: Spinner
    private lateinit var btnBankDelete: Button
    private lateinit var btnHome: Button
    private var accountNames: List<String> = emptyList() // Store account names
    private var selectedAccount: String? = null // Store selected account name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bankdelete_page)

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

        // Delete account button listener with confirmation dialog
        btnBankDelete.setOnClickListener {
            if (selectedAccount != null && userId != null) {
                showDeleteConfirmationDialog(userId, selectedAccount!!)
            } else {
                showToast("Please select an account to delete.")
            }
        }

        // Home button listener to navigate back to HomeActivity
        btnHome.setOnClickListener {
            navigateToHome()
        }
    }

    private fun initViews() {
        spinnerAccounts = findViewById(R.id.spnBankAccount)
        btnBankDelete = findViewById(R.id.btnBankDelete)
        btnHome = findViewById(R.id.btnHome) // Ensure this is defined in your layout
    }

    private fun setupSpinnerListener() {
        spinnerAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames.getOrNull(position) // Store the selected account
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }
    }

    // Fetch user accounts from the API
    private fun fetchUserAccounts(userId: String) {
        RetrofitClient.apiService.getUserAccounts(userId).enqueue(object : Callback<AccountsResponse> {
            override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    accountNames = response.body()!!.accounts.map { it.name }

                    // Populate the spinner with account names
                    val adapter = ArrayAdapter(
                        this@BankDeleteActivity,
                        android.R.layout.simple_spinner_item,
                        accountNames
                    )
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

    // Show confirmation dialog before deleting the account
    private fun showDeleteConfirmationDialog(userId: String, accountName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
            .setMessage("Are you sure you want to delete the account '$accountName'?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteAccount(userId, accountName) // Proceed with account deletion
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() } // Cancel the deletion process

        builder.create().show()
    }

    // Delete the selected account
    private fun deleteAccount(userId: String, accountName: String) {
        RetrofitClient.apiService.deleteAccount(userId, accountName).enqueue(object : Callback<DeleteAccountResponse> {
            override fun onResponse(call: Call<DeleteAccountResponse>, response: Response<DeleteAccountResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()!!.message)
                    // Refresh the spinner after deletion
                    fetchUserAccounts(userId)
                } else {
                    showToast("Failed to delete account")
                }
            }

            override fun onFailure(call: Call<DeleteAccountResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    // Navigate to HomeActivity
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish the current activity to remove it from the back stack
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
