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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.components.SafeDailySpendCard
import com.example.ui.components.TransactionRow
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.viewmodel.BudgetUiState
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun TallyHomeScreen(
  uiState: BudgetUiState,
  onOpenAddDialog: () -> Unit,
  onQuickAdd: (amount: Double, type: TransactionType, category: TransactionCategory, note: String) -> Unit,
  onEditTransaction: (TransactionItem) -> Unit,
  onDeleteTransaction: (TransactionItem) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = uiState.budgetSettings.currencySymbol
  val todayTally = uiState.todayTally
  val accountability = uiState.accountability

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "TallyWise",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (accountability != null && accountability.loggingStreakDays > 0) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AmberAccent.copy(alpha = 0.15f),
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.LocalFireDepartment,
                  contentDescription = null,
                  tint = AmberAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${accountability.loggingStreakDays}d streak",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = AmberAccent
                )
              }
            }
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Budget Settings")
          }
        }
      }
    }

    // Safe Daily Spend Accountability Hero
    if (accountability != null) {
      item {
        SafeDailySpendCard(
          metrics = accountability,
          onOpenSettings = onOpenSettings
        )
      }
    }

    // Today's Live Tally Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("today_tally_card"),
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
            Text(
              text = "Today's Running Tally",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            val netToday = todayTally?.netBalance ?: 0.0
            Text(
              text = "Net: ${if (netToday >= 0) "+" else ""}$currency${String.format("%.2f", netToday)}",
              style = MaterialTheme.typography.titleSmall,
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
              color = if (netToday >= 0) IncomeGreen else ExpenseRed
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Income Today Box
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = IncomeGreen.copy(alpha = 0.1f),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IncomeGreen.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Income",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "+$currency${String.format("%.2f", todayTally?.totalIncome ?: 0.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                  )
                }
              }
            }

            // Expense Today Box
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = ExpenseRed.copy(alpha = 0.1f),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ExpenseRed.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "-$currency${String.format("%.2f", todayTally?.totalExpense ?: 0.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                  )
                }
              }
            }
          }
        }
      }
    }

    // Quick One-Tap Add Presets
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Quick Tally Presets",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            Triple("☕ Chiya", 40.0, TransactionCategory.FOOD),
            Triple("🍱 Khaja", 250.0, TransactionCategory.FOOD),
            Triple("🚌 Bus", 35.0, TransactionCategory.TRANSPORT),
            Triple("🛒 Kirana", 850.0, TransactionCategory.SHOPPING)
          ).forEach { (label, amt, cat) ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  onQuickAdd(amt, TransactionType.EXPENSE, cat, label)
                }
                .testTag("preset_${label.take(4)}")
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1
                )
                Text(
                  text = "-$currency${amt.roundToInt()}",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  fontFamily = MonoFontFamily,
                  color = ExpenseRed
                )
              }
            }
          }
        }
      }
    }

    // Today's Transactions Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Today's Log (${todayTally?.transactions?.size ?: 0})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.clickable { onOpenAddDialog() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Add Entry",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // List of today's items or Empty State
    val todayItems = todayTally?.transactions.orEmpty()
    if (todayItems.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No tallies logged today yet",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap 'Add Entry' or use the quick presets above to tally your first spend or earnings!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(todayItems, key = { it.id }) { item ->
        TransactionRow(
          item = item,
          currencySymbol = currency,
          onEdit = onEditTransaction,
          onDelete = onDeleteTransaction
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}
