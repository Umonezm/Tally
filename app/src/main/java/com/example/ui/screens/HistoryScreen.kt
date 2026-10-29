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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionRow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun HistoryScreen(
  uiState: BudgetUiState,
  onSearchChange: (String) -> Unit,
  onTypeFilterChange: (TransactionType?) -> Unit,
  onCategoryFilterChange: (TransactionCategory?) -> Unit,
  onEditTransaction: (TransactionItem) -> Unit,
  onDeleteTransaction: (TransactionItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val currency = uiState.budgetSettings.currencySymbol
  val filteredItems = uiState.filteredTransactions

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Tally Log & History",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${filteredItems.size} entries found in current selection",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Search Bar
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search by note or category...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
          if (uiState.searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_search_field")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Type Filter Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = uiState.typeFilter == null,
          onClick = { onTypeFilterChange(null) },
          label = { Text("All Types") },
          modifier = Modifier.testTag("filter_all_types")
        )
        FilterChip(
          selected = uiState.typeFilter == TransactionType.EXPENSE,
          onClick = { onTypeFilterChange(if (uiState.typeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE) },
          label = { Text("Expenses Only") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
            selectedLabelColor = ExpenseRed
          ),
          modifier = Modifier.testTag("filter_expenses_only")
        )
        FilterChip(
          selected = uiState.typeFilter == TransactionType.INCOME,
          onClick = { onTypeFilterChange(if (uiState.typeFilter == TransactionType.INCOME) null else TransactionType.INCOME) },
          label = { Text("Income Only") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
            selectedLabelColor = IncomeGreen
          ),
          modifier = Modifier.testTag("filter_income_only")
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Category Horizontal Filter
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          FilterChip(
            selected = uiState.categoryFilter == null,
            onClick = { onCategoryFilterChange(null) },
            label = { Text("All Categories") }
          )
        }
        items(TransactionCategory.entries.toTypedArray()) { cat ->
          FilterChip(
            selected = uiState.categoryFilter == cat,
            onClick = { onCategoryFilterChange(if (uiState.categoryFilter == cat) null else cat) },
            label = { Text(cat.displayName) },
            leadingIcon = {
              Icon(imageVector = cat.icon, contentDescription = null, tint = cat.color, modifier = Modifier.size(16.dp))
            }
          )
        }
      }
    }

    // Filtered Summary Pill Banner
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
              text = "+$currency${String.format("%.2f", uiState.totalFilteredIncome)}",
              style = MaterialTheme.typography.labelMedium,
              fontFamily = MonoFontFamily,
              color = IncomeGreen,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "-$currency${String.format("%.2f", uiState.totalFilteredExpense)}",
              style = MaterialTheme.typography.labelMedium,
              fontFamily = MonoFontFamily,
              color = ExpenseRed,
              fontWeight = FontWeight.Bold
            )
          }

          val net = uiState.filteredNetBalance
          Text(
            text = "Net: ${if (net >= 0) "+" else ""}$currency${String.format("%.2f", net)}",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (net >= 0) IncomeGreen else ExpenseRed
          )
        }
      }
    }

    if (filteredItems.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No matching transactions found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Try adjusting your search query or category filters",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      // Group items by dateEpochDay
      val grouped = filteredItems.groupBy { it.dateEpochDay }
      grouped.keys.sortedDescending().forEach { epoch ->
        val dayItems = grouped[epoch].orEmpty()
        val firstItem = dayItems.first()

        item(key = "header_$epoch") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = firstItem.formattedDate,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val dayNet = dayItems.filter { it.type == TransactionType.INCOME }.sumOf { it.amount } -
              dayItems.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            Text(
              text = "${if (dayNet >= 0) "+" else ""}$currency${String.format("%.2f", dayNet)}",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = MonoFontFamily,
              color = if (dayNet >= 0) IncomeGreen else ExpenseRed
            )
          }
        }

        items(dayItems, key = { it.id }) { item ->
          TransactionRow(
            item = item,
            currencySymbol = currency,
            onEdit = onEditTransaction,
            onDelete = onDeleteTransaction
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}
