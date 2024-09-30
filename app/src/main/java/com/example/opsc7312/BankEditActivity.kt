package com.example.opsc7312

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.example.opsc7312.api.AccountsResponse
import com.example.opsc7312.api.RetrofitClient
import com.example.opsc7312.api.UpdateBalanceResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BankEditActivity : ComponentActivity() {

    private lateinit var btnHome: Button
    private lateinit var btnEdit: Button
    private lateinit var txtNewBalance: EditText
    private lateinit var spnBankAccount: Spinner
    private lateinit var userId: String // Non-nullable userId

    private var accountNames: List<String> = emptyList()
    private var selectedAccount: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bankedit_page)

        // Initialize UI elements
        initViews()
        retrieveUserId()

        // Set up spinner selection listener
        setupSpinnerListener()

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnEdit.setOnClickListener {
            updateAccountBalance()
        }
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnEdit = findViewById(R.id.btnBankEdit)
        txtNewBalance = findViewById(R.id.txtNewBalance)
        spnBankAccount = findViewById(R.id.spnBankAccount)
    }

    private fun retrieveUserId() {
        val sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val retrievedUserId = sharedPreferences.getString("userId", null)

        if (retrievedUserId != null) {
            userId = retrievedUserId
            fetchUserAccounts(userId)
        } else {
            showToast("User session is missing. Please log in again.")
            finish() // Close activity if no userId
        }
    }

    private fun setupSpinnerListener() {
        spnBankAccount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedAccount = accountNames.getOrNull(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAccount = null
            }
        }
    }

    private fun fetchUserAccounts(userId: String) {
        RetrofitClient.apiService.getUserAccounts(userId).enqueue(object : Callback<AccountsResponse> {
            override fun onResponse(call: Call<AccountsResponse>, response: Response<AccountsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    accountNames = response.body()!!.accounts.map { it.name }
                    populateSpinner()
                } else {
                    showToast("Failed to fetch accounts")
                }
            }

            override fun onFailure(call: Call<AccountsResponse>, t: Throwable) {
                showToast("Failed to connect: ${t.message}")
            }
        })
    }

    private fun populateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnBankAccount.adapter = adapter
    }

    private fun updateAccountBalance() {
        val accountName = spnBankAccount.selectedItem?.toString() ?: return
        val newBalance = txtNewBalance.text.toString().toDoubleOrNull()

        if (newBalance != null) {
            val call = RetrofitClient.apiService.updateAccountBalance(userId, accountName, newBalance)
            call.enqueue(object : Callback<UpdateBalanceResponse> {
                override fun onResponse(call: Call<UpdateBalanceResponse>, response: Response<UpdateBalanceResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        showToast(response.body()!!.message)
                    } else {
                        showToast("Failed to update balance")
                    }
                }

                override fun onFailure(call: Call<UpdateBalanceResponse>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })
        } else {
            showToast("Please enter a valid balance")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
