package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.BurnRatePoint
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DailyBurnBarChart(
  burnPoints: List<BurnRatePoint>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(burnPoints) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    selectedIndex = if (burnPoints.isNotEmpty()) burnPoints.lastIndex else null
  }

  val underBudgetCount = remember(burnPoints) { burnPoints.count { !it.isOverLimit } }
  val overBudgetCount = remember(burnPoints) { burnPoints.count { it.isOverLimit } }
  val dailyLimit = burnPoints.firstOrNull()?.dailyLimit ?: 80.0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("burn_rate_bar_card"),
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
        Column {
          Text(
            text = "Daily Burn Rate vs Target",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "$underBudgetCount days under target • $overBudgetCount days over",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Target badge
        val dailyLimit = burnPoints.firstOrNull()?.dailyLimit ?: 80.0
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
          Text(
            text = "Target: $currencySymbol${dailyLimit.roundToInt()}/d",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Inspection pill
      val active = selectedIndex?.let { if (it in burnPoints.indices) burnPoints[it] else null }
      if (active != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Day ${active.dateLabel}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$currencySymbol${active.expense.roundToInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = if (active.isOverLimit) ExpenseRed else IncomeGreen
              )

              Spacer(modifier = Modifier.width(8.dp))

              Text(
                text = if (active.isOverLimit) "Exceeded by $currencySymbol${(active.expense - active.dailyLimit).roundToInt()}" else "Safe ($currencySymbol${(active.dailyLimit - active.expense).roundToInt()} left)",
                style = MaterialTheme.typography.labelSmall,
                color = if (active.isOverLimit) ExpenseRed else IncomeGreen
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (burnPoints.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No daily burn data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val maxBurn = max(
          dailyLimit * 1.5,
          burnPoints.maxOf { it.expense } * 1.15
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(burnPoints) {
                detectTapGestures { offset ->
                  val count = burnPoints.size
                  if (count > 0) {
                    val barSlotWidth = size.width / count
                    val clicked = (offset.x / barSlotWidth).toInt().coerceIn(0, count - 1)
                    selectedIndex = clicked
                  }
                }
              }
          ) {
            val width = size.width
            val height = size.height
            val count = burnPoints.size
            val slotWidth = width / count
            val barWidth = (slotWidth * 0.65f).coerceIn(6f, 26f)
            val progress = animProgress.value

            // Draw Daily Limit dashed line
            val limitY = height - ((dailyLimit / maxBurn).toFloat() * height)
            drawLine(
              color = WarningOrange,
              start = Offset(0f, limitY),
              end = Offset(width, limitY),
              strokeWidth = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )

            // Draw bars
            burnPoints.forEachIndexed { i, pt ->
              val centerX = (i * slotWidth) + (slotWidth / 2)
              val barHeight = ((pt.expense / maxBurn).toFloat() * height * progress).coerceAtLeast(3f)
              val barTop = height - barHeight
              val isSelected = selectedIndex == i

              val barColor = when {
                pt.expense == 0.0 -> Color.LightGray.copy(alpha = 0.3f)
                pt.isOverLimit -> ExpenseRed
                else -> IncomeGreen
              }

              drawRoundRect(
                color = if (isSelected) barColor else barColor.copy(alpha = 0.85f),
                topLeft = Offset(centerX - (barWidth / 2), barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
              )

              if (isSelected) {
                // Glow marker on top of selected bar
                drawCircle(
                  color = Color.White,
                  radius = 3.dp.toPx(),
                  center = Offset(centerX, barTop - 4.dp.toPx())
                )
              }
            }
          }
        }

        // Legend row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(IncomeGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Under Target",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
              modifier = Modifier
                .size(8.dp)
                .background(ExpenseRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Over Target",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .width(14.dp)
                .height(2.dp)
                .background(WarningOrange)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Daily Limit",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
