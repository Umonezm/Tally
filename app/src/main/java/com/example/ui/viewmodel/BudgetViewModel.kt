package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.model.AccountabilityMetrics
import com.example.data.model.CategoryTally
import com.example.data.model.DailyTally
import com.example.data.model.PaceStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class TimeRangeFilter(val label: String) {
  THIS_WEEK("7 Days"),
  THIS_MONTH("This Month"),
  LAST_30_DAYS("30 Days"),
  ALL_TIME("All Time")
}

data class CashflowPoint(
  val epochDay: Long,
  val dateLabel: String,
  val income: Double,
  val expense: Double,
  val net: Double
)

data class BurnRatePoint(
  val epochDay: Long,
  val dateLabel: String,
  val expense: Double,
  val dailyLimit: Double,
  val isOverLimit: Boolean
)

/**
 * Dedicated ViewModel state storing the user-set monthly budget limit
 * and the reactive calculation for 'safe daily spend'.
 */
data class SafeDailySpendState(
  val monthlyBudgetLimit: Double = 50000.0,
  val totalSpentThisMonth: Double = 0.0,
  val remainingMonthlyBudget: Double = 50000.0,
  val daysInMonth: Int = 30,
  val currentDayOfMonth: Int = 1,
  val daysRemainingInMonth: Int = 30,
  val safeDailySpend: Double = 1666.66,
  val baseDailyAllowance: Double = 1666.66,
  val paceRatio: Float = 1.0f,
  val paceStatus: PaceStatus = PaceStatus.ON_TRACK,
  val isOverBudget: Boolean = false,
  val currencySymbol: String = "रू",
  val lastCalculatedMillis: Long = System.currentTimeMillis()
)

data class BudgetUiState(
  val transactions: List<TransactionItem> = emptyList(),
  val filteredTransactions: List<TransactionItem> = emptyList(),
  val budgetSettings: BudgetEntity = BudgetEntity(),
  val safeDailySpendState: SafeDailySpendState = SafeDailySpendState(),
  val todayTally: DailyTally? = null,
  val dailyTallies: List<DailyTally> = emptyList(),
  val categoryBreakdowns: List<CategoryTally> = emptyList(),
  val incomeBreakdowns: List<CategoryTally> = emptyList(),
  val accountability: AccountabilityMetrics? = null,
  val cashflowPoints: List<CashflowPoint> = emptyList(),
  val burnRatePoints: List<BurnRatePoint> = emptyList(),
  val timeRange: TimeRangeFilter = TimeRangeFilter.THIS_MONTH,
  val typeFilter: TransactionType? = null,
  val categoryFilter: TransactionCategory? = null,
  val searchQuery: String = "",
  val totalFilteredIncome: Double = 0.0,
  val totalFilteredExpense: Double = 0.0,
  val filteredNetBalance: Double = 0.0
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: BudgetRepository

  init {
    val database = AppDatabase.getDatabase(application)
    repository = BudgetRepository(database)
    viewModelScope.launch {
      repository.ensureSeeded()
    }
  }

  private val _timeRange = MutableStateFlow(TimeRangeFilter.THIS_MONTH)
  val timeRange: StateFlow<TimeRangeFilter> = _timeRange

  private val _typeFilter = MutableStateFlow<TransactionType?>(null)
  val typeFilter: StateFlow<TransactionType?> = _typeFilter

  private val _categoryFilter = MutableStateFlow<TransactionCategory?>(null)
  val categoryFilter: StateFlow<TransactionCategory?> = _categoryFilter

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery

  private data class FilterOptions(
    val timeRange: TimeRangeFilter,
    val typeFilter: TransactionType?,
    val categoryFilter: TransactionCategory?,
    val searchQuery: String
  )

  private val _filterOptions = combine(
    _timeRange,
    _typeFilter,
    _categoryFilter,
    _searchQuery
  ) { timeRange, typeFilter, categoryFilter, searchQuery ->
    FilterOptions(timeRange, typeFilter, categoryFilter, searchQuery)
  }

  val uiState: StateFlow<BudgetUiState> = combine(
    repository.allTransactions,
    repository.budgetSettings,
    _filterOptions
  ) { transactions, budget, filters ->
    calculateUiState(
      allTransactions = transactions,
      budget = budget,
      timeRange = filters.timeRange,
      typeFilter = filters.typeFilter,
      categoryFilter = filters.categoryFilter,
      searchQuery = filters.searchQuery
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = BudgetUiState()
  )

  /**
   * Dedicated StateFlow observing the user-set monthly budget limit from Room
   * and providing reactive 'safe daily spend' logic.
   */
  val safeDailySpendState: StateFlow<SafeDailySpendState> = combine(
    repository.allTransactions,
    repository.monthlyBudget
  ) { transactions, monthlyBudgetEntity ->
    calculateSafeDailySpend(
      allTransactions = transactions,
      monthlyBudgetLimit = monthlyBudgetEntity.budgetLimit,
      currencySymbol = monthlyBudgetEntity.currencySymbol
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SafeDailySpendState()
  )

  /**
   * Persists the user-set monthly budget limit in the Room database entity
   * and triggers recalculation of the safe daily spend.
   */
  fun setMonthlyBudgetLimit(limit: Double, currencySymbol: String = "रू") {
    viewModelScope.launch {
      repository.setMonthlyBudgetLimit(limit, currencySymbol)
    }
  }

  fun setTimeRange(range: TimeRangeFilter) {
    _timeRange.value = range
  }

  fun setTypeFilter(type: TransactionType?) {
    _typeFilter.value = type
  }

  fun setCategoryFilter(category: TransactionCategory?) {
    _categoryFilter.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun addTransaction(
    amount: Double,
    type: TransactionType,
    category: TransactionCategory,
    note: String,
    dateEpochDay: Long,
    paymentMethod: PaymentMethod = PaymentMethod.CARD
  ) {
    viewModelScope.launch {
      repository.insertTransaction(
        TransactionItem(
          amount = amount,
          type = type,
          category = category,
          note = note,
          dateEpochDay = dateEpochDay,
          paymentMethod = paymentMethod
        )
      )
    }
  }

  fun updateTransaction(item: TransactionItem) {
    viewModelScope.launch {
      repository.updateTransaction(item)
    }
  }

  fun deleteTransaction(item: TransactionItem) {
    viewModelScope.launch {
      repository.deleteTransaction(item)
    }
  }

  fun updateBudgetSettings(monthlyBudget: Double, savingsTarget: Double, currencySymbol: String) {
    viewModelScope.launch {
      repository.updateBudget(monthlyBudget, savingsTarget, currencySymbol)
    }
  }

  fun resetToSampleData() {
    viewModelScope.launch {
      val db = AppDatabase.getDatabase(getApplication())
      AppDatabase.seedInitialData(db)
    }
  }

  fun calculateSafeDailySpend(
    allTransactions: List<TransactionItem>,
    monthlyBudgetLimit: Double,
    currencySymbol: String = "रू"
  ): SafeDailySpendState {
    val today = LocalDate.now()
    val ym = YearMonth.from(today)
    val daysInMonth = ym.lengthOfMonth()
    val currentDayOfMonth = today.dayOfMonth
    val daysRemaining = max(1, daysInMonth - currentDayOfMonth + 1)

    val monthStartEpoch = today.withDayOfMonth(1).toEpochDay()
    val monthEndEpoch = today.withDayOfMonth(daysInMonth).toEpochDay()

    val totalSpentThisMonth = allTransactions
      .filter { it.dateEpochDay in monthStartEpoch..monthEndEpoch && it.type == TransactionType.EXPENSE }
      .sumOf { it.amount }

    val remainingMonthlyBudget = monthlyBudgetLimit - totalSpentThisMonth
    val safeDailySpend = max(0.0, remainingMonthlyBudget / daysRemaining)
    val baseDailyAllowance = monthlyBudgetLimit / daysInMonth

    val expectedSpendToDate = baseDailyAllowance * currentDayOfMonth
    val paceRatio = if (expectedSpendToDate > 0) (totalSpentThisMonth / expectedSpendToDate).toFloat() else 1f

    val paceStatus = when {
      totalSpentThisMonth > monthlyBudgetLimit -> PaceStatus.BUDGET_EXCEEDED
      paceRatio > 1.15f -> PaceStatus.OVER_PACE
      paceRatio in 0.85f..1.15f -> PaceStatus.ON_TRACK
      else -> PaceStatus.AHEAD_OF_BUDGET
    }

    return SafeDailySpendState(
      monthlyBudgetLimit = monthlyBudgetLimit,
      totalSpentThisMonth = totalSpentThisMonth,
      remainingMonthlyBudget = remainingMonthlyBudget,
      daysInMonth = daysInMonth,
      currentDayOfMonth = currentDayOfMonth,
      daysRemainingInMonth = daysRemaining,
      safeDailySpend = safeDailySpend,
      baseDailyAllowance = baseDailyAllowance,
      paceRatio = paceRatio,
      paceStatus = paceStatus,
      isOverBudget = totalSpentThisMonth > monthlyBudgetLimit,
      currencySymbol = currencySymbol,
      lastCalculatedMillis = System.currentTimeMillis()
    )
  }

  private fun calculateUiState(
    allTransactions: List<TransactionItem>,
    budget: BudgetEntity,
    timeRange: TimeRangeFilter,
    typeFilter: TransactionType?,
    categoryFilter: TransactionCategory?,
    searchQuery: String
  ): BudgetUiState {
    val safeSpendState = calculateSafeDailySpend(allTransactions, budget.monthlyBudget, budget.currencySymbol)
    val today = LocalDate.now()
    val todayEpoch = today.toEpochDay()
    val ym = YearMonth.from(today)
    val daysInMonth = ym.lengthOfMonth()
    val currentDayOfMonth = today.dayOfMonth
    val daysRemaining = max(1, daysInMonth - currentDayOfMonth + 1)

    // Month boundary for month-based budget accountability
    val monthStartEpoch = today.withDayOfMonth(1).toEpochDay()
    val monthEndEpoch = today.withDayOfMonth(daysInMonth).toEpochDay()

    val monthTransactions = allTransactions.filter { it.dateEpochDay in monthStartEpoch..monthEndEpoch }
    val totalSpentMonth = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncomeMonth = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val remainingBudget = budget.monthlyBudget - totalSpentMonth
    val safeDailySpend = max(0.0, remainingBudget / daysRemaining)

    // Daily limit guideline: monthly budget / days in month
    val baselineDailyBudget = budget.monthlyBudget / daysInMonth

    // Expected spend to date on a steady pace
    val expectedSpendToDate = baselineDailyBudget * currentDayOfMonth
    val paceRatio = if (expectedSpendToDate > 0) (totalSpentMonth / expectedSpendToDate).toFloat() else 1f

    val paceStatus = when {
      totalSpentMonth > budget.monthlyBudget -> PaceStatus.BUDGET_EXCEEDED
      paceRatio > 1.15f -> PaceStatus.OVER_PACE
      paceRatio in 0.85f..1.15f -> PaceStatus.ON_TRACK
      else -> PaceStatus.AHEAD_OF_BUDGET
    }

    val projectedMonthEndSpend = if (currentDayOfMonth > 0) {
      (totalSpentMonth / currentDayOfMonth) * daysInMonth
    } else {
      totalSpentMonth
    }

    // Streaks calculation: consecutive days ending today or yesterday where user logged transactions
    val loggedDaysSet = allTransactions.map { it.dateEpochDay }.toSet()
    var loggingStreak = 0
    var checkDay = todayEpoch
    if (!loggedDaysSet.contains(checkDay)) {
      checkDay = todayEpoch - 1
    }
    while (loggedDaysSet.contains(checkDay)) {
      loggingStreak++
      checkDay--
    }

    // Days this month where spend was <= baselineDailyBudget
    val monthDaysMap = monthTransactions
      .filter { it.type == TransactionType.EXPENSE }
      .groupBy { it.dateEpochDay }

    var underBudgetDaysCount = 0
    for (day in 1..currentDayOfMonth) {
      val epoch = today.withDayOfMonth(day).toEpochDay()
      val dayExpense = monthDaysMap[epoch]?.sumOf { it.amount } ?: 0.0
      if (dayExpense <= baselineDailyBudget) {
        underBudgetDaysCount++
      }
    }

    val savingsRate = if (totalIncomeMonth > 0) {
      val net = totalIncomeMonth - totalSpentMonth
      max(0f, ((net / totalIncomeMonth) * 100).toFloat())
    } else 0f

    // Composite financial health score (0-100)
    var score = 70
    if (paceStatus == PaceStatus.AHEAD_OF_BUDGET) score += 18
    else if (paceStatus == PaceStatus.ON_TRACK) score += 8
    else if (paceStatus == PaceStatus.OVER_PACE) score -= 15
    else if (paceStatus == PaceStatus.BUDGET_EXCEEDED) score -= 35

    if (savingsRate >= 25f) score += 10
    else if (savingsRate >= 15f) score += 5
    else if (savingsRate < 5f) score -= 5

    if (loggingStreak >= 3) score += min(10, loggingStreak * 2)

    val healthScore = score.coerceIn(10, 100)
    val healthGrade = when {
      healthScore >= 90 -> "A+"
      healthScore >= 80 -> "A"
      healthScore >= 70 -> "B"
      healthScore >= 60 -> "C"
      else -> "D"
    }

    val advice = when (paceStatus) {
      PaceStatus.AHEAD_OF_BUDGET -> "Outstanding budget discipline! You are spend-pacing below target. Your safe daily allowance is ${budget.currencySymbol}${safeDailySpend.roundToInt()}/day."
      PaceStatus.ON_TRACK -> "Balanced trajectory! You are on pace with your monthly target. Keep daily expenses under ${budget.currencySymbol}${safeDailySpend.roundToInt()} to maintain savings."
      PaceStatus.OVER_PACE -> "Spending velocity is elevated (${(paceRatio * 100).roundToInt()}% of pace). Aim to trim discretionary purchases over the next $daysRemaining days."
      PaceStatus.BUDGET_EXCEEDED -> "Monthly budget limit reached by ${budget.currencySymbol}${(totalSpentMonth - budget.monthlyBudget).roundToInt()}. Prioritize essential expenditures only."
    }

    val accountability = AccountabilityMetrics(
      monthlyBudget = budget.monthlyBudget,
      totalSpentMonth = totalSpentMonth,
      totalIncomeMonth = totalIncomeMonth,
      remainingBudget = remainingBudget,
      safeDailySpend = safeDailySpend,
      projectedMonthEndSpend = projectedMonthEndSpend,
      daysInMonth = daysInMonth,
      currentDayOfMonth = currentDayOfMonth,
      daysRemaining = daysRemaining,
      pacePercentage = paceRatio,
      paceStatus = paceStatus,
      loggingStreakDays = loggingStreak,
      underBudgetDaysMonth = underBudgetDaysCount,
      savingsRatePercentage = savingsRate,
      healthScore = healthScore,
      healthGrade = healthGrade,
      actionableAdvice = advice,
      currencySymbol = budget.currencySymbol
    )

    // Range filtering for analytics & list
    val cutoffEpoch = when (timeRange) {
      TimeRangeFilter.THIS_WEEK -> today.minusDays(6).toEpochDay()
      TimeRangeFilter.THIS_MONTH -> monthStartEpoch
      TimeRangeFilter.LAST_30_DAYS -> today.minusDays(29).toEpochDay()
      TimeRangeFilter.ALL_TIME -> Long.MIN_VALUE
    }

    val inRangeTransactions = allTransactions.filter { it.dateEpochDay >= cutoffEpoch }

    // Search and category/type filtered for transaction view
    val filteredTransactions = inRangeTransactions.filter { item ->
      val matchesType = typeFilter == null || item.type == typeFilter
      val matchesCategory = categoryFilter == null || item.category == categoryFilter
      val matchesSearch = searchQuery.isBlank() ||
        item.note.contains(searchQuery, ignoreCase = true) ||
        item.category.displayName.contains(searchQuery, ignoreCase = true)
      matchesType && matchesCategory && matchesSearch
    }

    val totalFilteredIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalFilteredExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val filteredNetBalance = totalFilteredIncome - totalFilteredExpense

    // Today's tally
    val todayTransactions = allTransactions.filter { it.dateEpochDay == todayEpoch }
    val todayIncome = todayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val todayExpense = todayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val todayTally = DailyTally(
      dateEpochDay = todayEpoch,
      date = today,
      formattedDate = "Today, " + today.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")),
      totalIncome = todayIncome,
      totalExpense = todayExpense,
      netBalance = todayIncome - todayExpense,
      transactions = todayTransactions
    )

    // Daily tallies for list / history
    val groupedByDay = inRangeTransactions.groupBy { it.dateEpochDay }
    val dailyTallies = groupedByDay.keys.sortedDescending().map { epoch ->
      val list = groupedByDay[epoch].orEmpty()
      val date = LocalDate.ofEpochDay(epoch)
      val dayIncome = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
      val dayExpense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
      DailyTally(
        dateEpochDay = epoch,
        date = date,
        formattedDate = when (epoch) {
          todayEpoch -> "Today, ${date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}"
          todayEpoch - 1 -> "Yesterday, ${date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}"
          else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM dd"))
        },
        totalIncome = dayIncome,
        totalExpense = dayExpense,
        netBalance = dayIncome - dayExpense,
        transactions = list
      )
    }

    // Category Breakdowns for Expenses
    val expenseList = inRangeTransactions.filter { it.type == TransactionType.EXPENSE }
    val totalExpenseInRange = expenseList.sumOf { it.amount }
    val categoryBreakdowns = expenseList.groupBy { it.category }.map { (category, items) ->
      val sum = items.sumOf { it.amount }
      val pct = if (totalExpenseInRange > 0) (sum / totalExpenseInRange).toFloat() else 0f
      CategoryTally(
        category = category,
        totalAmount = sum,
        percentage = pct,
        count = items.size
      )
    }.sortedByDescending { it.totalAmount }

    // Category Breakdowns for Income
    val incomeList = inRangeTransactions.filter { it.type == TransactionType.INCOME }
    val totalIncomeInRange = incomeList.sumOf { it.amount }
    val incomeBreakdowns = incomeList.groupBy { it.category }.map { (category, items) ->
      val sum = items.sumOf { it.amount }
      val pct = if (totalIncomeInRange > 0) (sum / totalIncomeInRange).toFloat() else 0f
      CategoryTally(
        category = category,
        totalAmount = sum,
        percentage = pct,
        count = items.size
      )
    }.sortedByDescending { it.totalAmount }

    // Chart Points (Chronological order)
    val sortedDays = groupedByDay.keys.sorted()
    val cashflowPoints = sortedDays.map { epoch ->
      val list = groupedByDay[epoch].orEmpty()
      val date = LocalDate.ofEpochDay(epoch)
      val inc = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
      val exp = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
      CashflowPoint(
        epochDay = epoch,
        dateLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")),
        income = inc,
        expense = exp,
        net = inc - exp
      )
    }

    val burnRatePoints = sortedDays.map { epoch ->
      val list = groupedByDay[epoch].orEmpty()
      val date = LocalDate.ofEpochDay(epoch)
      val exp = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
      BurnRatePoint(
        epochDay = epoch,
        dateLabel = date.format(java.time.format.DateTimeFormatter.ofPattern("dd")),
        expense = exp,
        dailyLimit = baselineDailyBudget,
        isOverLimit = exp > baselineDailyBudget
      )
    }

    return BudgetUiState(
      transactions = allTransactions,
      filteredTransactions = filteredTransactions,
      budgetSettings = budget,
      safeDailySpendState = safeSpendState,
      todayTally = todayTally,
      dailyTallies = dailyTallies,
      categoryBreakdowns = categoryBreakdowns,
      incomeBreakdowns = incomeBreakdowns,
      accountability = accountability,
      cashflowPoints = cashflowPoints,
      burnRatePoints = burnRatePoints,
      timeRange = timeRange,
      typeFilter = typeFilter,
      categoryFilter = categoryFilter,
      searchQuery = searchQuery,
      totalFilteredIncome = totalFilteredIncome,
      totalFilteredExpense = totalFilteredExpense,
      filteredNetBalance = filteredNetBalance
    )
  }
}
