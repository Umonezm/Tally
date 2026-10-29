package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.AccountabilityMetrics
import com.example.data.model.PaceStatus
import com.example.ui.components.AccountabilityStreaksCard
import com.example.ui.components.SafeDailySpendCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PurpleCard
import com.example.ui.viewmodel.BudgetUiState
import kotlin.math.roundToInt

@Composable
fun AccountabilityScreen(
  uiState: BudgetUiState,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val metrics = uiState.accountability
  val currency = uiState.budgetSettings.currencySymbol

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Budget Accountability",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Discipline tracking, spending pace & health grades",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        OutlinedButton(
          onClick = onOpenSettings,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Goals", style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    if (metrics != null) {
      // Safe Daily Spend Hero
      item {
        SafeDailySpendCard(
          metrics = metrics,
          onOpenSettings = onOpenSettings
        )
      }

      // Streaks & Health Score Card
      item {
        AccountabilityStreaksCard(
          metrics = metrics
        )
      }

      // Burn Pace Projection Meter
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("burn_pace_projection_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(InfoBlue.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Spending Velocity Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Projected outcome at current pace",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forecast comparison row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Budget Target",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "$currency${metrics.monthlyBudget.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              val isOver = metrics.projectedMonthEndSpend > metrics.monthlyBudget
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isOver) ExpenseRed.copy(alpha = 0.12f) else IncomeGreen.copy(alpha = 0.12f),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "Projected Month-End",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "$currency${metrics.projectedMonthEndSpend.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) ExpenseRed else IncomeGreen
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pace percentage indicator
            val pacePct = (metrics.pacePercentage * 100).roundToInt()
            Text(
              text = "Burn Rate Velocity: $pacePct% of permitted pace",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = if (metrics.paceStatus == PaceStatus.AHEAD_OF_BUDGET) IncomeGreen else if (metrics.paceStatus == PaceStatus.ON_TRACK) InfoBlue else ExpenseRed
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
              progress = { (metrics.pacePercentage / 1.5f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
              color = if (metrics.paceStatus == PaceStatus.AHEAD_OF_BUDGET) IncomeGreen else if (metrics.paceStatus == PaceStatus.ON_TRACK) InfoBlue else ExpenseRed,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      }

      // Accountability Rules Checklist
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
              text = "Daily Discipline Protocol",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            listOf(
              "Log every transaction immediately after payment" to (metrics.loggingStreakDays > 0),
              "Check Safe Daily Spend before discretionary purchases" to true,
              "Maintain current spend pace under $currency${metrics.safeDailySpend.roundToInt()}/day" to (metrics.paceStatus != PaceStatus.OVER_PACE && metrics.paceStatus != PaceStatus.BUDGET_EXCEEDED),
              "Target at least 15% monthly savings rate" to (metrics.savingsRatePercentage >= 15f)
            ).forEach { (rule, isHonored) ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = if (isHonored) IncomeGreen else MaterialTheme.colorScheme.outline,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = rule,
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isHonored) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
