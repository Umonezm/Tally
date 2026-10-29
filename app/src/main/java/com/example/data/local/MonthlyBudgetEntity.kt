package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity defining and storing the user-set monthly budget limit.
 * Used to calculate the 'safe daily spend' logic:
 * Safe Daily Spend = (Monthly Budget Limit - Total Spent This Month) / Max(1, Days Remaining)
 */
@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
  @PrimaryKey
  val monthKey: String = "CURRENT", // "CURRENT" or formatted year-month "YYYY-MM"
  val budgetLimit: Double = 50000.0, // Default monthly budget in Nepali Rupees
  val currencySymbol: String = "रू", // Nepali Rupees (रू / Rs.)
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis()
)
