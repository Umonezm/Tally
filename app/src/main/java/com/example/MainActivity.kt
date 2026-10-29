package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionItem
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.components.BudgetSettingsDialog
import com.example.ui.screens.AccountabilityScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TallyHomeScreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val icon: ImageVector) {
  TALLY("Tally", Icons.Default.ReceiptLong),
  ANALYTICS("Graphs", Icons.Default.BarChart),
  ACCOUNTABILITY("Accountability", Icons.Default.Security),
  HISTORY("History", Icons.Default.History)
}

class MainActivity : ComponentActivity() {
  private val viewModel: BudgetViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        TallyApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallyApp(viewModel: BudgetViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentTab by remember { mutableStateOf(MainTab.TALLY) }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  var showAddSheet by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<TransactionItem?>(null) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<TransactionItem?>(null) }

  // Back handler: Return to Tally Home if on secondary tab
  BackHandler(enabled = currentTab != MainTab.TALLY) {
    currentTab = MainTab.TALLY
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
      ) {
        MainTab.entries.forEach { tab ->
          val selected = currentTab == tab
          NavigationBarItem(
            selected = selected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = tab.title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
              )
            },
            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
          )
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          editingItem = null
          showAddSheet = true
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_add_entry")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Log Expense or Income",
          modifier = Modifier.size(28.dp)
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentTab) {
        MainTab.TALLY -> TallyHomeScreen(
          uiState = uiState,
          onOpenAddDialog = {
            editingItem = null
            showAddSheet = true
          },
          onQuickAdd = { amount, type, category, note ->
            viewModel.addTransaction(
              amount = amount,
              type = type,
              category = category,
              note = note,
              dateEpochDay = java.time.LocalDate.now().toEpochDay()
            )
            scope.launch {
              snackbarHostState.showSnackbar("Logged $note (-${uiState.budgetSettings.currencySymbol}$amount)")
            }
          },
          onEditTransaction = { item ->
            editingItem = item
            showAddSheet = true
          },
          onDeleteTransaction = { item ->
            itemToDelete = item
          },
          onOpenSettings = { showSettingsDialog = true }
        )

        MainTab.ANALYTICS -> AnalyticsScreen(
          uiState = uiState,
          onSelectTimeRange = { viewModel.setTimeRange(it) }
        )

        MainTab.ACCOUNTABILITY -> AccountabilityScreen(
          uiState = uiState,
          onOpenSettings = { showSettingsDialog = true }
        )

        MainTab.HISTORY -> HistoryScreen(
          uiState = uiState,
          onSearchChange = { viewModel.setSearchQuery(it) },
          onTypeFilterChange = { viewModel.setTypeFilter(it) },
          onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
          onEditTransaction = { item ->
            editingItem = item
            showAddSheet = true
          },
          onDeleteTransaction = { item ->
            itemToDelete = item
          }
        )
      }
    }
  }

  // Add / Edit Transaction Sheet
  if (showAddSheet) {
    AddTransactionBottomSheet(
      sheetState = sheetState,
      currencySymbol = uiState.budgetSettings.currencySymbol,
      initialItem = editingItem,
      onDismiss = { showAddSheet = false },
      onSave = { amount, type, category, note, dateEpochDay, paymentMethod ->
        val currentEdit = editingItem
        if (currentEdit != null) {
          viewModel.updateTransaction(
            currentEdit.copy(
              amount = amount,
              type = type,
              category = category,
              note = note,
              dateEpochDay = dateEpochDay,
              paymentMethod = paymentMethod
            )
          )
          scope.launch {
            snackbarHostState.showSnackbar("Entry updated successfully")
          }
        } else {
          viewModel.addTransaction(
            amount = amount,
            type = type,
            category = category,
            note = note,
            dateEpochDay = dateEpochDay,
            paymentMethod = paymentMethod
          )
          scope.launch {
            snackbarHostState.showSnackbar("Tally entry recorded")
          }
        }
        showAddSheet = false
      }
    )
  }

  // Budget Goals Settings Dialog
  if (showSettingsDialog) {
    BudgetSettingsDialog(
      currentSettings = uiState.budgetSettings,
      onDismiss = { showSettingsDialog = false },
      onSave = { monthlyBudget, savingsTarget, currencySymbol ->
        viewModel.updateBudgetSettings(monthlyBudget, savingsTarget, currencySymbol)
        showSettingsDialog = false
        scope.launch {
          snackbarHostState.showSnackbar("Budget goals updated")
        }
      },
      onResetSampleData = {
        viewModel.resetToSampleData()
        scope.launch {
          snackbarHostState.showSnackbar("Sample tallies refreshed")
        }
      }
    )
  }

  // Delete Confirmation Dialog
  itemToDelete?.let { item ->
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text("Delete Tally Entry?") },
      text = {
        Text("Are you sure you want to delete '${if (item.note.isNotBlank()) item.note else item.category.displayName}' for ${uiState.budgetSettings.currencySymbol}${item.amount}?")
      },
      confirmButton = {
        TextButton(
          onClick = {
            viewModel.deleteTransaction(item)
            itemToDelete = null
            scope.launch {
              snackbarHostState.showSnackbar("Entry deleted")
            }
          }
        ) {
          Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
