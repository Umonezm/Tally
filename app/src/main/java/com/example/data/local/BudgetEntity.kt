package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_settings")
data class BudgetEntity(
  @PrimaryKey
  val id: Int = 1,
  val monthlyBudget: Double = 50000.0, // In Nepali Rupees
  val savingsTarget: Double = 15000.0,
  val currencySymbol: String = "रू"
)
