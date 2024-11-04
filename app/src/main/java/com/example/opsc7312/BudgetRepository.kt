// BudgetRepository.kt
package com.example.opsc7312

import android.content.Context
import com.example.opsc7312.AppDatabase
import com.example.opsc7312.BudgetEntity
import com.example.opsc7312.api.AddCategoryRequest
import com.example.opsc7312.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BudgetRepository(context: Context) {
    private val budgetDao = AppDatabase.getDatabase(context).budgetDao()

    suspend fun addOrUpdateBudgetOffline(budgetEntity: BudgetEntity) {
        budgetDao.insertBudgetAction(budgetEntity)
    }

    suspend fun syncBudgets() {
        val pendingActions = budgetDao.getPendingActions()
        for (action in pendingActions) {
            try {
                when (action.actionType) {
                    "create" -> {
                        val response = RetrofitClient.apiService.addCategory(
                            action.userId,
                            action.accountName,
                            AddCategoryRequest(
                                category = action.category,
                                amountBudgeted = action.amountBudgeted,
                                amountSpent = action.amountSpent
                            )
                        ).execute()

                        if (response.isSuccessful) {
                            action.isSynced = true
                            budgetDao.updateBudgetAction(action)
                        }
                    }
                    "edit" -> {
                        val responseBudget = RetrofitClient.apiService.editBudgetAmount(
                            action.userId,
                            action.accountName,
                            action.category,
                            action.amountBudgeted
                        ).execute()

                        val responseSpent = RetrofitClient.apiService.editSpentAmount(
                            action.userId,
                            action.accountName,
                            action.category,
                            action.amountSpent
                        ).execute()

                        if (responseBudget.isSuccessful && responseSpent.isSuccessful) {
                            action.isSynced = true
                            budgetDao.updateBudgetAction(action)
                        }
                    }
                    "delete" -> {
                        val response = RetrofitClient.apiService.deleteCategory(
                            action.userId,
                            action.accountName,
                            action.category
                        ).execute()

                        if (response.isSuccessful) {
                            budgetDao.deleteBudgetActionById(action.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // Log exception or handle retries
            }
        }
    }
}
