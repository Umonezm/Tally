package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.ChartColor1
import com.example.ui.theme.ChartColor2
import com.example.ui.theme.ChartColor3
import com.example.ui.theme.ChartColor4
import com.example.ui.theme.ChartColor5
import com.example.ui.theme.ChartColor6
import com.example.ui.theme.ChartColor7
import com.example.ui.theme.ChartColor8
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PurpleCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TransactionType {
  EXPENSE,
  INCOME
}

enum class TransactionCategory(
  val displayName: String,
  val defaultType: TransactionType,
  val icon: ImageVector,
  val color: Color
) {
  FOOD("Food & Dining", TransactionType.EXPENSE, Icons.Default.Restaurant, ChartColor1),
  TRANSPORT("Transport & Travel", TransactionType.EXPENSE, Icons.Default.DirectionsCar, ChartColor2),
  HOUSING("Housing & Rent", TransactionType.EXPENSE, Icons.Default.Home, ChartColor3),
  BILLS("Bills & Utilities", TransactionType.EXPENSE, Icons.Default.ReceiptLong, ChartColor4),
  SHOPPING("Shopping & Goods", TransactionType.EXPENSE, Icons.Default.ShoppingCart, ChartColor5),
  HEALTH("Health & Medical", TransactionType.EXPENSE, Icons.Default.LocalHospital, ChartColor6),
  ENTERTAINMENT("Entertainment", TransactionType.EXPENSE, Icons.Default.Movie, ChartColor7),
  
  SALARY("Salary & Wages", TransactionType.INCOME, Icons.Default.Work, IncomeGreen),
  FREELANCE("Freelance & Gigs", TransactionType.INCOME, Icons.Default.Laptop, InfoBlue),
  INVESTMENT("Investments", TransactionType.INCOME, Icons.Default.TrendingUp, PurpleCard),
  GIFT("Gifts & Grants", TransactionType.INCOME, Icons.Default.CardGiftcard, AmberAccent),
  
  OTHER("Other", TransactionType.EXPENSE, Icons.Default.Category, ChartColor8);

  companion object {
    fun fromName(name: String): TransactionCategory {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
    }
  }
}

enum class PaymentMethod(val label: String) {
  CARD("Credit/Debit Card"),
  CASH("Cash"),
  DIGITAL_WALLET("Digital Wallet / UPI"),
  BANK_TRANSFER("Bank Transfer");

  companion object {
    fun fromName(name: String): PaymentMethod {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: CARD
    }
  }
}

data class TransactionItem(
  val id: Long = 0,
  val amount: Double,
  val type: TransactionType,
  val category: TransactionCategory,
  val note: String,
  val dateEpochDay: Long,
  val paymentMethod: PaymentMethod = PaymentMethod.CARD,
  val isRecurring: Boolean = false
) {
  val date: LocalDate
    get() = LocalDate.ofEpochDay(dateEpochDay)

  val formattedDate: String
    get() = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

  val shortDate: String
    get() = date.format(DateTimeFormatter.ofPattern("MMM dd"))
}

data class DailyTally(
  val dateEpochDay: Long,
  val date: LocalDate,
  val formattedDate: String,
  val totalIncome: Double,
  val totalExpense: Double,
  val netBalance: Double,
  val transactions: List<TransactionItem>
)

data class CategoryTally(
  val category: TransactionCategory,
  val totalAmount: Double,
  val percentage: Float,
  val count: Int
)

enum class PaceStatus {
  AHEAD_OF_BUDGET, // Far below pace -> Excellent!
  ON_TRACK,        // Right around expected pace
  OVER_PACE,       // Spending faster than daily allowance
  BUDGET_EXCEEDED  // Total spend > monthly budget
}

data class AccountabilityMetrics(
  val monthlyBudget: Double,
  val totalSpentMonth: Double,
  val totalIncomeMonth: Double,
  val remainingBudget: Double,
  val safeDailySpend: Double,
  val projectedMonthEndSpend: Double,
  val daysInMonth: Int,
  val currentDayOfMonth: Int,
  val daysRemaining: Int,
  val pacePercentage: Float, // e.g. 0.85 = 85% of allowed pace
  val paceStatus: PaceStatus,
  val loggingStreakDays: Int,
  val underBudgetDaysMonth: Int,
  val savingsRatePercentage: Float,
  val healthScore: Int,
  val healthGrade: String,
  val actionableAdvice: String,
  val currencySymbol: String = "रू"
)
