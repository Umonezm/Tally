package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.BudgetEntity
import com.example.ui.theme.MonoFontFamily

@Composable
fun BudgetSettingsDialog(
  currentSettings: BudgetEntity,
  onDismiss: () -> Unit,
  onSave: (monthlyBudget: Double, savingsTarget: Double, currencySymbol: String) -> Unit,
  onResetSampleData: () -> Unit
) {
  var budgetInput by remember { mutableStateOf(currentSettings.monthlyBudget.toInt().toString()) }
  var savingsInput by remember { mutableStateOf(currentSettings.savingsTarget.toInt().toString()) }
  var selectedCurrency by remember { mutableStateOf(currentSettings.currencySymbol) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Budget & Accountability Goals",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Set your financial thresholds to calculate safe daily spend, burn pace, and adherence grades.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Currency Symbol Selector
        Column {
          Text(
            text = "Preferred Currency",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("रू", "Rs.", "$", "€", "£", "₹").forEach { symbol ->
              FilterChip(
                selected = selectedCurrency == symbol,
                onClick = { selectedCurrency = symbol },
                label = { Text(symbol, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) }
              )
            }
          }
        }

        // Monthly Budget
        OutlinedTextField(
          value = budgetInput,
          onValueChange = { budgetInput = it },
          label = { Text("Monthly Budget Limit") },
          leadingIcon = { Text(selectedCurrency, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_limit_input")
        )

        // Savings Target
        OutlinedTextField(
          value = savingsInput,
          onValueChange = { savingsInput = it },
          label = { Text("Monthly Savings Target") },
          leadingIcon = { Text(selectedCurrency, fontFamily = MonoFontFamily, fontWeight = FontWeight.Bold) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("savings_target_input")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Reset Sample Data
        OutlinedButton(
          onClick = {
            onResetSampleData()
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Load / Refresh Sample Tallies")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val budget = budgetInput.toDoubleOrNull() ?: currentSettings.monthlyBudget
          val savings = savingsInput.toDoubleOrNull() ?: currentSettings.savingsTarget
          onSave(budget, savings, selectedCurrency)
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("save_budget_button")
      ) {
        Text("Save Goals")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
