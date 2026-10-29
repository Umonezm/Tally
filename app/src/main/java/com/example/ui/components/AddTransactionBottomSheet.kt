package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionBottomSheet(
  sheetState: SheetState,
  currencySymbol: String,
  initialItem: TransactionItem? = null,
  onDismiss: () -> Unit,
  onSave: (amount: Double, type: TransactionType, category: TransactionCategory, note: String, dateEpochDay: Long, paymentMethod: PaymentMethod) -> Unit
) {
  var selectedType by remember { mutableStateOf(initialItem?.type ?: TransactionType.EXPENSE) }
  var amountInput by remember { mutableStateOf(initialItem?.let { String.format("%.2f", it.amount) } ?: "") }
  var selectedCategory by remember {
    mutableStateOf(
      initialItem?.category ?: if (selectedType == TransactionType.EXPENSE) TransactionCategory.FOOD else TransactionCategory.SALARY
    )
  }
  var noteInput by remember { mutableStateOf(initialItem?.note ?: "") }
  var selectedPaymentMethod by remember { mutableStateOf(initialItem?.paymentMethod ?: PaymentMethod.CARD) }
  var selectedDateEpoch by remember { mutableStateOf(initialItem?.dateEpochDay ?: LocalDate.now().toEpochDay()) }
  var errorText by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp)
        .imePadding()
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (initialItem != null) "Edit Entry" else "New Tally Entry",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Expense vs Income Segmented Toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(4.dp)
      ) {
        val isExpense = selectedType == TransactionType.EXPENSE
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isExpense) ExpenseRed else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedType = TransactionType.EXPENSE
              if (selectedCategory.defaultType != TransactionType.EXPENSE) {
                selectedCategory = TransactionCategory.FOOD
              }
            }
            .testTag("toggle_expense")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Expense (-)",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (!isExpense) IncomeGreen else Color.Transparent,
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedType = TransactionType.INCOME
              if (selectedCategory.defaultType != TransactionType.INCOME) {
                selectedCategory = TransactionCategory.SALARY
              }
            }
            .testTag("toggle_income")
        ) {
          Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Income (+)",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Amount Input Field
      Text(
        text = "Amount",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = amountInput,
        onValueChange = {
          amountInput = it
          errorText = null
        },
        leadingIcon = {
          Text(
            text = currencySymbol,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen,
            modifier = Modifier.padding(start = 12.dp)
          )
        },
        placeholder = {
          Text(
            text = "0.00",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = MonoFontFamily,
            color = MaterialTheme.colorScheme.outline
          )
        },
        textStyle = MaterialTheme.typography.titleLarge.copy(
          fontFamily = MonoFontFamily,
          fontWeight = FontWeight.Bold
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = errorText != null,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("amount_input_field")
      )

      if (errorText != null) {
        Text(
          text = errorText ?: "",
          color = ExpenseRed,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
      }

      // Quick amount chips
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(50, 100, 250, 500, 1000).forEach { value ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
              .clickable {
                val current = amountInput.toDoubleOrNull() ?: 0.0
                amountInput = String.format("%.2f", current + value)
              }
              .testTag("quick_amt_$value")
          ) {
            Text(
              text = "+$currencySymbol$value",
              style = MaterialTheme.typography.labelSmall,
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Categories
      Text(
        text = "Category",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      val categoriesForType = TransactionCategory.entries.filter {
        if (selectedType == TransactionType.EXPENSE) it.defaultType == TransactionType.EXPENSE
        else it.defaultType == TransactionType.INCOME || it == TransactionCategory.OTHER
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categoriesForType.forEach { cat ->
          val isSelected = selectedCategory == cat
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, cat.color) else null,
            modifier = Modifier
              .clickable { selectedCategory = cat }
              .testTag("cat_select_${cat.name}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = cat.icon,
                contentDescription = null,
                tint = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = cat.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Note input
      Text(
        text = "Note / Description",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      OutlinedTextField(
        value = noteInput,
        onValueChange = { noteInput = it },
        placeholder = { Text("e.g. Lunch with team, Groceries, Client payment") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_input_field")
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Date Selection
      Text(
        text = "Date",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      val today = LocalDate.now()
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "Today" to today.toEpochDay(),
          "Yesterday" to today.minusDays(1).toEpochDay(),
          "2 Days Ago" to today.minusDays(2).toEpochDay()
        ).forEach { (label, epoch) ->
          val isSelected = selectedDateEpoch == epoch
          FilterChip(
            selected = isSelected,
            onClick = { selectedDateEpoch = epoch },
            label = { Text(label) },
            leadingIcon = if (isSelected) {
              { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else {
              { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) }
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Payment Method
      Text(
        text = "Payment Method",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        PaymentMethod.entries.forEach { method ->
          FilterChip(
            selected = selectedPaymentMethod == method,
            onClick = { selectedPaymentMethod = method },
            label = { Text(method.label) }
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      Button(
        onClick = {
          val parsedAmount = amountInput.toDoubleOrNull()
          if (parsedAmount == null || parsedAmount <= 0.0) {
            errorText = "Please enter a valid positive amount."
            return@Button
          }
          onSave(
            parsedAmount,
            selectedType,
            selectedCategory,
            noteInput.trim(),
            selectedDateEpoch,
            selectedPaymentMethod
          )
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedType == TransactionType.EXPENSE) MaterialTheme.colorScheme.primary else IncomeGreen
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_transaction_button")
      ) {
        Text(
          text = if (initialItem != null) "Update Tally Entry" else "Save Tally Entry",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
