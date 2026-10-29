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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MonoFontFamily
import com.example.ui.viewmodel.CashflowPoint
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CashflowChart(
  points: List<CashflowPoint>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(points) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    selectedIndex = if (points.isNotEmpty()) points.lastIndex else null
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("cashflow_chart_card"),
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
            text = "Cashflow Dynamics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Daily Income vs Expense Inflow",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Legends
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(IncomeGreen, CircleShape)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Inflow",
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
            text = "Outflow",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Inspection tooltip when a point is selected
      val activePoint = selectedIndex?.let { if (it in points.indices) points[it] else null }
      if (activePoint != null) {
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
            Column {
              Text(
                text = activePoint.dateLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              val net = activePoint.net
              Text(
                text = "Net: ${if (net >= 0) "+" else ""}$currencySymbol${net.roundToInt()}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = MonoFontFamily,
                color = if (net >= 0) IncomeGreen else ExpenseRed
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Income",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "+$currencySymbol${activePoint.income.roundToInt()}",
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = MonoFontFamily,
                  color = IncomeGreen,
                  fontWeight = FontWeight.Bold
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Expense",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "-$currencySymbol${activePoint.expense.roundToInt()}",
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = MonoFontFamily,
                  color = ExpenseRed,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (points.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No cashflow records in this timeframe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        val maxVal = max(
          100.0,
          points.maxOf { max(it.income, it.expense) } * 1.15
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
        ) {
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(points) {
                detectTapGestures { offset ->
                  val count = points.size
                  if (count > 0) {
                    val stepX = size.width / max(1, count - 1)
                    val clickedIndex = (offset.x / stepX)
                      .roundToInt()
                      .coerceIn(0, count - 1)
                    selectedIndex = clickedIndex
                  }
                }
              }
          ) {
            val width = size.width
            val height = size.height
            val count = points.size
            val stepX = if (count > 1) width / (count - 1) else width
            val progress = animProgress.value

            // Draw horizontal guide lines
            val gridLines = 4
            for (i in 0..gridLines) {
              val y = height * (i.toFloat() / gridLines)
              drawLine(
                color = Color.LightGray.copy(alpha = 0.25f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
              )
            }

            // Path for Income
            val incomePath = Path()
            val incomeAreaPath = Path()

            // Path for Expense
            val expensePath = Path()
            val expenseAreaPath = Path()

            val incomeCoords = mutableListOf<Offset>()
            val expenseCoords = mutableListOf<Offset>()

            points.forEachIndexed { i, p ->
              val x = if (count == 1) width / 2 else i * stepX
              val yInc = height - ((p.income / maxVal).toFloat() * height * progress)
              val yExp = height - ((p.expense / maxVal).toFloat() * height * progress)

              incomeCoords.add(Offset(x, yInc))
              expenseCoords.add(Offset(x, yExp))

              if (i == 0) {
                incomePath.moveTo(x, yInc)
                incomeAreaPath.moveTo(x, height)
                incomeAreaPath.lineTo(x, yInc)

                expensePath.moveTo(x, yExp)
                expenseAreaPath.moveTo(x, height)
                expenseAreaPath.lineTo(x, yExp)
              } else {
                incomePath.lineTo(x, yInc)
                incomeAreaPath.lineTo(x, yInc)

                expensePath.lineTo(x, yExp)
                expenseAreaPath.lineTo(x, yExp)
              }
            }

            val lastX = if (count == 1) width / 2 else (count - 1) * stepX
            incomeAreaPath.lineTo(lastX, height)
            incomeAreaPath.close()

            expenseAreaPath.lineTo(lastX, height)
            expenseAreaPath.close()

            // Draw area gradient for income
            drawPath(
              path = incomeAreaPath,
              brush = Brush.verticalGradient(
                colors = listOf(IncomeGreen.copy(alpha = 0.15f), IncomeGreen.copy(alpha = 0.01f)),
                startY = 0f,
                endY = height
              )
            )

            // Draw area gradient for expense
            drawPath(
              path = expenseAreaPath,
              brush = Brush.verticalGradient(
                colors = listOf(ExpenseRed.copy(alpha = 0.12f), ExpenseRed.copy(alpha = 0.01f)),
                startY = 0f,
                endY = height
              )
            )

            // Draw Income line
            drawPath(
              path = incomePath,
              color = IncomeGreen,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Expense line
            drawPath(
              path = expensePath,
              color = ExpenseRed,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Highlight selected point
            selectedIndex?.let { idx ->
              if (idx in points.indices) {
                val incPt = incomeCoords[idx]
                val expPt = expenseCoords[idx]

                // Vertical cursor line
                drawLine(
                  color = Color.Gray.copy(alpha = 0.4f),
                  start = Offset(incPt.x, 0f),
                  end = Offset(incPt.x, height),
                  strokeWidth = 1.5.dp.toPx()
                )

                // Income dot
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = incPt)
                drawCircle(color = IncomeGreen, radius = 4.dp.toPx(), center = incPt)

                // Expense dot
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = expPt)
                drawCircle(color = ExpenseRed, radius = 4.dp.toPx(), center = expPt)
              }
            }
          }
        }

        // Bottom axis labels
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (points.isNotEmpty()) {
            Text(
              text = points.first().dateLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (points.size > 2) {
              Text(
                text = points[points.size / 2].dateLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = points.last().dateLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
