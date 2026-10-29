package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.MonthlyBudgetDao
import com.example.data.local.MonthlyBudgetEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BudgetRepository(
  private val database: AppDatabase,
  private val transactionDao: TransactionDao = database.transactionDao(),
  private val budgetDao: BudgetDao = database.budgetDao(),
  private val monthlyBudgetDao: MonthlyBudgetDao = database.monthlyBudgetDao()
) {
  val allTransactions: Flow<List<TransactionItem>> = transactionDao
    .getAllTransactions()
    .map { list -> list.map { it.toDomain() } }
    .flowOn(Dispatchers.IO)

  val budgetSettings: Flow<BudgetEntity> = budgetDao
    .getBudget()
    .map { it ?: BudgetEntity() }
    .flowOn(Dispatchers.IO)

  val monthlyBudget: Flow<MonthlyBudgetEntity> = monthlyBudgetDao
    .getCurrentMonthlyBudget()
    .map { it ?: MonthlyBudgetEntity() }
    .flowOn(Dispatchers.IO)

  suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
    if (transactionDao.getCount() == 0) {
      AppDatabase.seedInitialData(database)
    }
  }

  suspend fun insertTransaction(item: TransactionItem): Long = withContext(Dispatchers.IO) {
    transactionDao.insert(TransactionEntity.fromDomain(item))
  }

  suspend fun updateTransaction(item: TransactionItem) = withContext(Dispatchers.IO) {
    transactionDao.update(TransactionEntity.fromDomain(item))
  }

  suspend fun deleteTransaction(item: TransactionItem) = withContext(Dispatchers.IO) {
    transactionDao.delete(TransactionEntity.fromDomain(item))
  }

  suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
    transactionDao.deleteById(id)
  }

  suspend fun setMonthlyBudgetLimit(
    limit: Double,
    currencySymbol: String = "$",
    monthKey: String = "CURRENT"
  ) = withContext(Dispatchers.IO) {
    monthlyBudgetDao.insertOrUpdate(
      MonthlyBudgetEntity(
        monthKey = monthKey,
        budgetLimit = limit,
        currencySymbol = currencySymbol,
        updatedAtMillis = System.currentTimeMillis()
      )
    )
    budgetDao.insertOrUpdate(
      BudgetEntity(
        id = 1,
        monthlyBudget = limit,
        currencySymbol = currencySymbol
      )
    )
  }

  suspend fun updateBudget(monthlyBudget: Double, savingsTarget: Double, currencySymbol: String) =
    withContext(Dispatchers.IO) {
      setMonthlyBudgetLimit(monthlyBudget, currencySymbol)
      budgetDao.insertOrUpdate(
        BudgetEntity(
          id = 1,
          monthlyBudget = monthlyBudget,
          savingsTarget = savingsTarget,
          currencySymbol = currencySymbol
        )
      )
    }
}
