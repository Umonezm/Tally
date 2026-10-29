package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountabilityMetrics
import com.example.data.model.PaceStatus
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.PurpleCard
import kotlin.math.roundToInt

@Composable
fun SafeDailySpendCard(
  metrics: AccountabilityMetrics,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = metrics.currencySymbol
  val statusColor = when (metrics.paceStatus) {
    PaceStatus.AHEAD_OF_BUDGET -> IncomeGreen
    PaceStatus.ON_TRACK -> InfoBlue
    PaceStatus.OVER_PACE -> AmberAccent
    PaceStatus.BUDGET_EXCEEDED -> ExpenseRed
  }

  val statusLabel = when (metrics.paceStatus) {
    PaceStatus.AHEAD_OF_BUDGET -> "Ahead of Target"
    PaceStatus.ON_TRACK -> "On Track"
    PaceStatus.OVER_PACE -> "Pacing Above Limit"
    PaceStatus.BUDGET_EXCEEDED -> "Budget Exhausted"
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("safe_daily_spend_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
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
              .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Safe Daily Spend",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${metrics.daysRemaining} days left in month",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = statusColor.copy(alpha = 0.15f)
        ) {
          Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Giant Safe Allowance Number
      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "$currency${metrics.safeDailySpend.roundToInt()}",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (metrics.safeDailySpend > 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
          )
          Text(
            text = " / day",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Remaining",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$currency${metrics.remainingBudget.roundToInt()}",
            style = MaterialTheme.typography.titleMedium,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (metrics.remainingBudget >= 0) IncomeGreen else ExpenseRed
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Budget consumption vs Time progress
      val budgetFraction = (metrics.totalSpentMonth / metrics.monthlyBudget).toFloat().coerceIn(0f, 1f)
      val timeFraction = (metrics.currentDayOfMonth.toFloat() / metrics.daysInMonth).coerceIn(0f, 1f)

      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Budget Used: ${(budgetFraction * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Month Elapsed: ${(timeFraction * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          // Time passed marker line
          Box(
            modifier = Modifier
              .fillMaxWidth(budgetFraction)
              .height(10.dp)
              .background(
                Brush.horizontalGradient(
                  listOf(
                    IncomeGreen,
                    if (budgetFraction > 0.85f) ExpenseRed else AmberAccent
                  )
                )
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Actionable advice banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = metrics.actionableAdvice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Composable
fun AccountabilityStreaksCard(
  metrics: AccountabilityMetrics,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("accountability_streaks_card"),
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
        text = "Accountability & Discipline",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Real-time metrics measuring your consistency",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Logging Streak Box
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = AmberAccent.copy(alpha = 0.12f),
          modifier = Modifier
            .weight(1f)
            .testTag("streak_metric_box")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AmberAccent.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "${metrics.loggingStreakDays} Days",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = AmberAccent
            )
            Text(
              text = "Logging Streak",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Under Budget Days Box
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = IncomeGreen.copy(alpha = 0.12f),
          modifier = Modifier
            .weight(1f)
            .testTag("under_budget_box")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IncomeGreen.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = IncomeGreen,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "${metrics.underBudgetDaysMonth} Days",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = IncomeGreen
            )
            Text(
              text = "Under Target",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Financial Health Grade Box
        val gradeColor = when (metrics.healthGrade) {
          "A+", "A" -> IncomeGreen
          "B" -> InfoBlue
          "C" -> AmberAccent
          else -> ExpenseRed
        }
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = gradeColor.copy(alpha = 0.12f),
          modifier = Modifier
            .weight(1f)
            .testTag("health_grade_box")
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(gradeColor.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = metrics.healthGrade,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = gradeColor
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "${metrics.healthScore}/100",
              style = MaterialTheme.typography.titleLarge,
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = gradeColor
            )
            Text(
              text = "Health Score",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Badges Row
      Text(
        text = "Accountability Milestones",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AccountabilityBadgeItem(
          title = "Active Logger",
          subtitle = "3+ Day Streak",
          isUnlocked = metrics.loggingStreakDays >= 3,
          color = AmberAccent,
          icon = Icons.Default.LocalFireDepartment,
          modifier = Modifier.weight(1f)
        )

        AccountabilityBadgeItem(
          title = "Thrifty Guard",
          subtitle = "5+ Days Under",
          isUnlocked = metrics.underBudgetDaysMonth >= 5,
          color = IncomeGreen,
          icon = Icons.Default.Security,
          modifier = Modifier.weight(1f)
        )

        AccountabilityBadgeItem(
          title = "Surplus Builder",
          subtitle = ">15% Saved",
          isUnlocked = metrics.savingsRatePercentage >= 15f,
          color = PurpleCard,
          icon = Icons.Default.ElectricBolt,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun AccountabilityBadgeItem(
  title: String,
  subtitle: String,
  isUnlocked: Boolean,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isUnlocked) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isUnlocked) color else Color.Gray.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = if (isUnlocked) color else Color.Gray
      )
    }
  }
}
