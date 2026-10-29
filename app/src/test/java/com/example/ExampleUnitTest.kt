package com.example

import com.example.data.local.MonthlyBudgetEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.SafeDailySpendState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun transactionItem_mappingAndFormatting() {
    val date = LocalDate.of(2026, 10, 29)
    val item = TransactionItem(
      id = 1,
      amount = 450.0,
      type = TransactionType.EXPENSE,
      category = TransactionCategory.FOOD,
      note = "Dinner with friends",
      dateEpochDay = date.toEpochDay(),
      paymentMethod = PaymentMethod.CARD
    )

    assertEquals("Dinner with friends", item.note)
    assertEquals(TransactionType.EXPENSE, item.type)
    assertEquals(TransactionCategory.FOOD, item.category)
    assertEquals("Oct 29", item.shortDate)
    assertTrue(item.amount > 0)
  }

  @Test
  fun monthlyBudgetEntity_nepaliRupeesDefault() {
    val entity = MonthlyBudgetEntity(
      monthKey = "2026-10",
      budgetLimit = 50000.0,
      currencySymbol = "रू"
    )

    assertEquals("2026-10", entity.monthKey)
    assertEquals(50000.0, entity.budgetLimit, 0.001)
    assertEquals("रू", entity.currencySymbol)
  }

  @Test
  fun safeDailySpendState_nepaliRupeesCalculation() {
    val monthlyBudget = 45000.0
    val totalSpent = 15000.0
    val remainingBudget = monthlyBudget - totalSpent // 30000.0
    val daysRemaining = 15
    val safeDailySpend = remainingBudget / daysRemaining // 2000.0

    val state = SafeDailySpendState(
      monthlyBudgetLimit = monthlyBudget,
      totalSpentThisMonth = totalSpent,
      remainingMonthlyBudget = remainingBudget,
      daysInMonth = 30,
      currentDayOfMonth = 16,
      daysRemainingInMonth = daysRemaining,
      safeDailySpend = safeDailySpend,
      baseDailyAllowance = monthlyBudget / 30,
      isOverBudget = false,
      currencySymbol = "रू"
    )

    assertEquals(30000.0, state.remainingMonthlyBudget, 0.001)
    assertEquals(2000.0, state.safeDailySpend, 0.001)
    assertEquals(1500.0, state.baseDailyAllowance, 0.001)
    assertEquals("रू", state.currencySymbol)
    assertFalse(state.isOverBudget)
  }
}
