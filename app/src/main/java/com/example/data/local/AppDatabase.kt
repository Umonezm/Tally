package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
  entities = [TransactionEntity::class, BudgetEntity::class, MonthlyBudgetEntity::class],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun budgetDao(): BudgetDao
  abstract fun monthlyBudgetDao(): MonthlyBudgetDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "tally_wise_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(DatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            seedInitialData(database)
          }
        }
      }
    }

    suspend fun seedInitialData(database: AppDatabase) {
      val budgetDao = database.budgetDao()
      val monthlyBudgetDao = database.monthlyBudgetDao()
      val transactionDao = database.transactionDao()

      budgetDao.insertOrUpdate(
        BudgetEntity(
          id = 1,
          monthlyBudget = 50000.0,
          savingsTarget = 15000.0,
          currencySymbol = "रू"
        )
      )

      monthlyBudgetDao.insertOrUpdate(
        MonthlyBudgetEntity(
          monthKey = "CURRENT",
          budgetLimit = 50000.0,
          currencySymbol = "रू"
        )
      )

      val today = LocalDate.now()
      val samples = listOf(
        // Today's tallies
        TransactionEntity(
          amount = 120.00,
          type = "EXPENSE",
          category = "FOOD",
          note = "Morning Masala Chiya & Samosa",
          dateEpochDay = today.toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        TransactionEntity(
          amount = 450.00,
          type = "EXPENSE",
          category = "FOOD",
          note = "Thakali Khana Set Lunch",
          dateEpochDay = today.toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        TransactionEntity(
          amount = 18500.00,
          type = "INCOME",
          category = "FREELANCE",
          note = "Tech Consulting & UI Design Gig",
          dateEpochDay = today.toEpochDay(),
          paymentMethod = "BANK_TRANSFER"
        ),
        // Yesterday
        TransactionEntity(
          amount = 75.00,
          type = "EXPENSE",
          category = "TRANSPORT",
          note = "Sajha Yatayat & Micro Bus Commute",
          dateEpochDay = today.minusDays(1).toEpochDay(),
          paymentMethod = "CASH"
        ),
        TransactionEntity(
          amount = 3200.00,
          type = "EXPENSE",
          category = "SHOPPING",
          note = "Bhatbhateni Supermarket Groceries",
          dateEpochDay = today.minusDays(1).toEpochDay(),
          paymentMethod = "CARD"
        ),
        // Day -2
        TransactionEntity(
          amount = 350.00,
          type = "EXPENSE",
          category = "FOOD",
          note = "Steam Momo & Cold Drink",
          dateEpochDay = today.minusDays(2).toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        TransactionEntity(
          amount = 450.00,
          type = "EXPENSE",
          category = "ENTERTAINMENT",
          note = "QFX Cinema Movie Ticket",
          dateEpochDay = today.minusDays(2).toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        // Day -3
        TransactionEntity(
          amount = 1750.00,
          type = "EXPENSE",
          category = "BILLS",
          note = "WorldLink High-Speed Fiber Internet",
          dateEpochDay = today.minusDays(3).toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        TransactionEntity(
          amount = 220.00,
          type = "EXPENSE",
          category = "FOOD",
          note = "Local Bakery Bread & Milk",
          dateEpochDay = today.minusDays(3).toEpochDay(),
          paymentMethod = "CASH"
        ),
        // Day -4
        TransactionEntity(
          amount = 850.00,
          type = "EXPENSE",
          category = "HEALTH",
          note = "Pharmacy Vitamins & Health Care",
          dateEpochDay = today.minusDays(4).toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        ),
        TransactionEntity(
          amount = 4500.00,
          type = "INCOME",
          category = "INVESTMENT",
          note = "NEPSE Stock Dividend Return",
          dateEpochDay = today.minusDays(4).toEpochDay(),
          paymentMethod = "BANK_TRANSFER"
        ),
        // Day -5
        TransactionEntity(
          amount = 1100.00,
          type = "EXPENSE",
          category = "SHOPPING",
          note = "Warm Winter Sweater",
          dateEpochDay = today.minusDays(5).toEpochDay(),
          paymentMethod = "CARD"
        ),
        // Day -6
        TransactionEntity(
          amount = 620.00,
          type = "EXPENSE",
          category = "FOOD",
          note = "Fresh Vegetable Tarkari Market",
          dateEpochDay = today.minusDays(6).toEpochDay(),
          paymentMethod = "CASH"
        ),
        // Earlier this month
        TransactionEntity(
          amount = 65000.00,
          type = "INCOME",
          category = "SALARY",
          note = "Monthly Primary Salary Deposit",
          dateEpochDay = today.withDayOfMonth(1).toEpochDay(),
          paymentMethod = "BANK_TRANSFER"
        ),
        TransactionEntity(
          amount = 16000.00,
          type = "EXPENSE",
          category = "HOUSING",
          note = "Apartment Flat Rent (Ghar Bhada)",
          dateEpochDay = today.withDayOfMonth(1).toEpochDay(),
          paymentMethod = "BANK_TRANSFER"
        ),
        TransactionEntity(
          amount = 1250.00,
          type = "EXPENSE",
          category = "BILLS",
          note = "Nepal Electricity Authority (NEA) Bill",
          dateEpochDay = today.withDayOfMonth(5).toEpochDay(),
          paymentMethod = "DIGITAL_WALLET"
        )
      )
      transactionDao.insertAll(samples)
    }
  }
}
