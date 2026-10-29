package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {
  @Query("SELECT * FROM monthly_budgets WHERE monthKey = :monthKey LIMIT 1")
  fun getBudgetForMonth(monthKey: String): Flow<MonthlyBudgetEntity?>

  @Query("SELECT * FROM monthly_budgets WHERE monthKey = 'CURRENT' LIMIT 1")
  fun getCurrentMonthlyBudget(): Flow<MonthlyBudgetEntity?>

  @Query("SELECT budgetLimit FROM monthly_budgets WHERE monthKey = 'CURRENT' LIMIT 1")
  fun getCurrentBudgetLimit(): Flow<Double?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(budget: MonthlyBudgetEntity)

  @Query("UPDATE monthly_budgets SET budgetLimit = :limit, updatedAtMillis = :timestamp WHERE monthKey = :monthKey")
  suspend fun updateBudgetLimit(
    monthKey: String,
    limit: Double,
    timestamp: Long = System.currentTimeMillis()
  )
}
