package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CashflowChart
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailyBurnBarChart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.TimeRangeFilter
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
  uiState: BudgetUiState,
  onSelectTimeRange: (TimeRangeFilter) -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = uiState.budgetSettings.currencySymbol
  val totalInc = uiState.totalFilteredIncome
  val totalExp = uiState.totalFilteredExpense
  val net = totalInc - totalExp
  val savingsRate = if (totalInc > 0) ((net / totalInc) * 100).coerceAtLeast(0.0) else 0.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Range Selector
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Financial Analytics & Graphs",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Comprehensive visual breakdowns of cashflow & spending",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Time Range Filter Segment
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(4.dp)
      ) {
        TimeRangeFilter.entries.forEach { range ->
          val isSelected = uiState.timeRange == range
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier
              .weight(1f)
              .clickable { onSelectTimeRange(range) }
              .testTag("filter_range_${range.name}")
          ) {
            Box(
              modifier = Modifier.padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = range.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // High Level Metric Overview Cards
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "Period Cashflow Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Income
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = IncomeGreen.copy(alpha = 0.1f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "+$currency${totalInc.roundToInt()}",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = MonoFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = IncomeGreen
                )
              }
            }

            // Expense
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = ExpenseRed.copy(alpha = 0.1f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "-$currency${totalExp.roundToInt()}",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = MonoFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = ExpenseRed
                )
              }
            }

            // Savings Rate
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Saved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${savingsRate.roundToInt()}%",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = MonoFontFamily,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }
    }

    // Graph 1: Cashflow Dynamics Line & Area Chart
    item {
      CashflowChart(
        points = uiState.cashflowPoints,
        currencySymbol = currency
      )
    }

    // Graph 2: Category Donut Chart
    item {
      CategoryDonutChart(
        categories = uiState.categoryBreakdowns,
        currencySymbol = currency
      )
    }

    // Graph 3: Daily Burn Rate vs Daily Target Allowance
    item {
      DailyBurnBarChart(
        burnPoints = uiState.burnRatePoints,
        currencySymbol = currency
      )
    }

    // Ranked Categories Spenders List
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Text(
            text = "Category Breakdown Ranking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (uiState.categoryBreakdowns.isEmpty()) {
            Text(
              text = "No category data for this timeframe",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            uiState.categoryBreakdowns.forEach { cat ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cat.category.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = cat.category.icon,
                    contentDescription = null,
                    tint = cat.category.color,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = cat.category.displayName,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = "$currency${cat.totalAmount.roundToInt()}",
                      style = MaterialTheme.typography.titleSmall,
                      fontFamily = MonoFontFamily,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  // Percentage bar
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth(cat.percentage)
                        .height(6.dp)
                        .background(cat.category.color)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}
