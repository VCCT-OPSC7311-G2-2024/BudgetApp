package com.example.opsc7312

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_actions")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val accountName: String,
    val category: String,
    val amountBudgeted: Double,
    val amountSpent: Double,
    val actionType: String, // "create", "edit", "delete"
    var isSynced: Boolean = false
)