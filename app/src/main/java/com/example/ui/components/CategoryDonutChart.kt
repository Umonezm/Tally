package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryTally
import com.example.ui.theme.MonoFontFamily
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  categories: List<CategoryTally>,
  currencySymbol: String,
  title: String = "Spending by Category",
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<CategoryTally?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(categories) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    selectedCategory = categories.firstOrNull()
  }

  val totalAmount = remember(categories) { categories.sumOf { it.totalAmount } }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("category_donut_card"),
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
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${categories.size} categories active",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = "$currencySymbol${totalAmount.roundToInt()}",
          style = MaterialTheme.typography.titleMedium,
          fontFamily = MonoFontFamily,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (categories.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No category data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        // Donut Visual Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(
            modifier = Modifier.size(190.dp)
          ) {
            val strokeWidth = 28.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)

            var startAngle = -90f
            val progress = animProgress.value

            categories.forEach { cat ->
              val sweep = cat.percentage * 360f * progress
              if (sweep > 0.5f) {
                // Gap spacing between slices
                val gap = if (categories.size > 1) 2.5f else 0f
                val effectiveSweep = (sweep - gap).coerceAtLeast(0.5f)

                val isSelected = selectedCategory?.category == cat.category
                val currentStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth

                drawArc(
                  color = cat.category.color,
                  startAngle = startAngle + (gap / 2),
                  sweepAngle = effectiveSweep,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = currentStroke, cap = StrokeCap.Round)
                )
              }
              startAngle += sweep
            }
          }

          // Center Display
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            val current = selectedCategory ?: categories.firstOrNull()
            if (current != null) {
              Text(
                text = current.category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
              )
              Text(
                text = "$currencySymbol${current.totalAmount.roundToInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "${(current.percentage * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = current.category.color,
                fontWeight = FontWeight.SemiBold
              )
            } else {
              Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol${totalAmount.roundToInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Interactive Legend Chips
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          categories.forEach { item ->
            val isSelected = selectedCategory?.category == item.category
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) item.category.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, item.category.color) else null,
              modifier = Modifier
                .clickable { selectedCategory = item }
                .testTag("cat_chip_${item.category.name}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(item.category.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.category.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${(item.percentage * 100).roundToInt()}%",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = MonoFontFamily,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}
