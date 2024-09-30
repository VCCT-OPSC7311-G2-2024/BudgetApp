package com.example.opsc7312.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.Locale

// Request body for login
data class LoginRequest(
    val username: String,
    val password: String
)

// Response from login
data class LoginResponse(
    val message: String,
    val userId: String,
    val email: String
)

// Registration request body
data class RegisterRequest(
    val username: String,
    val password: String, // This will be the hashed password
    val email: String
)

// Registration response body
data class RegisterResponse(
    val message: String
)

// Request body for adding an account
data class AddAccountRequest(
    val name: String,
    val type: String,
    val amount: Double,
    val budgets: List<Budget> = emptyList()  // Optional budgets list (empty by default)
)

// Response body for adding an account
data class AddAccountResponse(
    val message: String,
    val user: User
)

// Data class representing a user
data class User(
    val id: String?,
    val username: String,
    val email: String,
    val accounts: List<Account>
)

// Data class representing an account
data class Account(
    val name: String,
    val type: String,
    val amount: Double,
    val budgets: List<Budget>
)


data class AccountsResponse(
    val message: String,
    val accounts: List<Account>
)


data class CategoriesResponse(
    val message: String,
    val account: String,
    val budgets: List<Budget>,
    val categories: List<Category>
)

data class Category(
    val id: String,
    val name: String
)

// Data class representing a budget
data class Budget(
    val category: String,
    val amountBudgeted: Double,
    val amountSpent: Double
)

// Request body for adding a category
data class AddCategoryRequest(
    val category: String,
    val amountBudgeted: Double,
    val amountSpent: Double = 0.0  // Initial amount spent is 0
)

// Response body for adding a category
data class AddCategoryResponse(
    val message: String
)

// Data class for budget amount update response
data class UpdateBudgetAmountResponse(
    val message: String,
    val newBudgetedAmount: Double
)

// Data class for spent amount update response
data class UpdateSpentAmountResponse(
    val message: String,
    val newSpentAmount: Double
)

// Response data class for account deletion
data class DeleteAccountResponse(
    val message: String
)

data class UpdateBalanceResponse(
    val message: String,
    val newBalance: Double
)

interface ApiService {
    @POST("/api/user/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/api/user/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/user/{userId}/addAccount")
    fun addAccount(
        @Path("userId") userId: String,
        @Body request: AddAccountRequest
    ): Call<AddAccountResponse>

    @POST("/api/user/{userId}/account/{accountName}/addCategory")
    fun addCategory(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String,
        @Body request: AddCategoryRequest
    ): Call<AddCategoryResponse>

    @GET("api/user/{userId}/accounts")
    fun getUserAccounts(@Path("userId") userId: String): Call<AccountsResponse>

    @PUT("/api/user/{userId}/account/{accountName}/budget/{category}/editBudgetAmount")
    fun editBudgetAmount(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String,
        @Path("category") category: String,
        @Body newAmount: Double
    ): Call<UpdateBudgetAmountResponse>

    // Update the spent amount for a category
    @PUT("/api/user/{userId}/account/{accountName}/budget/{category}/editSpentAmount")
    fun editSpentAmount(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String,
        @Path("category") category: String,
        @Body newAmount: Double
    ): Call<UpdateSpentAmountResponse>

    @DELETE("/api/user/{userId}/account/{accountName}/deleteAccount")
    fun deleteAccount(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String
    ): Call<DeleteAccountResponse>

    @DELETE("/api/user/{userId}/account/{accountName}/budget/{category}/deleteCategory")
    fun deleteCategory(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String,
        @Path("category") category: String
    ): Call<Void>

    // API call to get categories for an account
    @GET("/api/user/{userId}/account/{accountName}/categories")
    fun getAccountCategories(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String
    ): Call<CategoriesResponse>

    @PUT("/api/user/{userId}/account/{accountName}/updateBalance")
    fun updateAccountBalance(
        @Path("userId") userId: String,
        @Path("accountName") accountName: String,
        @Body newBalance: Double
    ): Call<UpdateBalanceResponse>
}
