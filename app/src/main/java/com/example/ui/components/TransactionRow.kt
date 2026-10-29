package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily

@Composable
fun TransactionRow(
  item: TransactionItem,
  currencySymbol: String,
  onEdit: (TransactionItem) -> Unit,
  onDelete: (TransactionItem) -> Unit,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = modifier
      .fillMaxWidth()
      .testTag("transaction_row_${item.id}")
      .clickable { showMenu = true }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon Squircle
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(item.category.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = item.category.icon,
          contentDescription = item.category.displayName,
          tint = item.category.color,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Note & Metadata
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = if (item.note.isNotBlank()) item.note else item.category.displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = item.category.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Box(
            modifier = Modifier
              .size(3.dp)
              .background(MaterialTheme.colorScheme.outline, CircleShape)
          )

          Text(
            text = item.paymentMethod.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Amount Display
      val isIncome = item.type == TransactionType.INCOME
      Column(
        horizontalAlignment = Alignment.End
      ) {
        Text(
          text = "${if (isIncome) "+" else "-"}$currencySymbol${String.format("%.2f", item.amount)}",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = MonoFontFamily,
          fontWeight = FontWeight.Bold,
          color = if (isIncome) IncomeGreen else ExpenseRed
        )

        Text(
          text = item.shortDate,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Options menu
      Box {
        IconButton(
          onClick = { showMenu = true },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("Edit Entry") },
            onClick = {
              showMenu = false
              onEdit(item)
            },
            leadingIcon = {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            }
          )
          DropdownMenuItem(
            text = { Text("Delete Entry", color = ExpenseRed) },
            onClick = {
              showMenu = false
              onDelete(item)
            },
            leadingIcon = {
              Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
            }
          )
        }
      }
    }
  }
}
