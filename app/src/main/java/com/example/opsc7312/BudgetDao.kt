package com.example.opsc7312

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudgetAction(budgetEntity: BudgetEntity)

    @Query("SELECT * FROM budget_actions WHERE isSynced = 0")
    suspend fun getPendingActions(): List<BudgetEntity>

    @Update
    suspend fun updateBudgetAction(budgetEntity: BudgetEntity)

    @Query("DELETE FROM budget_actions WHERE id = :id")
    suspend fun deleteBudgetActionById(id: Int)
}
