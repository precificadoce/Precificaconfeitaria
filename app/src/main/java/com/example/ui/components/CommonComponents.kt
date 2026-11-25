package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.PricingCalculator
import com.example.domain.RecipePricingBreakdown
import com.example.ui.theme.ChartFee
import com.example.ui.theme.ChartIngredients
import com.example.ui.theme.ChartLabor
import com.example.ui.theme.ChartOverhead
import com.example.ui.theme.ChartPackaging
import com.example.ui.theme.ChartProfit
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = PricingCalculator.formatBrl(amount),
        modifier = modifier,
        color = color,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionTestTag: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onActionClick != null) {
            androidx.compose.material3.TextButton(
                onClick = onActionClick,
                modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val selected = category == selectedCategory
            FilterChip(
                selected = selected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_$category")
            )
        }
    }
}

@Composable
fun CostStackedBar(
    breakdown: RecipePricingBreakdown,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val total = breakdown.finalSellingTotal.takeIf { it > 0.0 } ?: 1.0
    val segments = listOf(
        ChartIngredients to (breakdown.ingredientsCost / total).toFloat(),
        ChartOverhead to (breakdown.overheadCost / total).toFloat(),
        ChartLabor to (breakdown.laborCost / total).toFloat(),
        ChartPackaging to (breakdown.packagingCost / total).toFloat(),
        ChartFee to (breakdown.cardFeeCost / total).toFloat(),
        ChartProfit to (breakdown.desiredProfit / total).toFloat()
    ).filter { it.second > 0.005f }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
    ) {
        var currentX = 0f
        segments.forEach { (color, fraction) ->
            val segmentWidth = size.width * fraction
            drawRect(
                color = color,
                topLeft = Offset(currentX, 0f),
                size = Size(segmentWidth, size.height)
            )
            currentX += segmentWidth
        }
    }
}

@Composable
fun CostDonutChart(
    breakdown: RecipePricingBreakdown,
    modifier: Modifier = Modifier,
    chartSize: Dp = 176.dp
) {
    val total = breakdown.finalSellingTotal.takeIf { it > 0.0 } ?: 1.0
    val slices = listOf(
        Triple("Ingredientes", ChartIngredients, breakdown.ingredientsCost),
        Triple("Gás / Energia", ChartOverhead, breakdown.overheadCost),
        Triple("Mão de Obra", ChartLabor, breakdown.laborCost),
        Triple("Embalagem", ChartPackaging, breakdown.packagingCost),
        Triple("Taxa Cartão", ChartFee, breakdown.cardFeeCost),
        Triple("Lucro Líquido", ChartProfit, breakdown.desiredProfit)
    ).filter { it.third > 0.0 }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.size(chartSize)) {
                val strokeWidth = 22.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                if (slices.isEmpty()) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    var startAngle = -90f
                    slices.forEach { (_, color, value) ->
                        val sweep = ((value / total) * 360.0).toFloat()
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Venda Lote",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MoneyText(
                    amount = breakdown.finalSellingTotal,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEach { (label, color, value) ->
                val pct = (value / total) * 100.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = PricingCalculator.formatPercent(pct),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStatePanel(
    title: String,
    subtitle: String,
    primaryButtonLabel: String,
    onPrimaryClick: () -> Unit,
    primaryTestTag: String,
    secondaryButtonLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    secondaryTestTag: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_recipes),
                contentDescription = stringResource(R.string.empty_recipes_desc),
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(primaryTestTag)
            ) {
                Text(text = primaryButtonLabel)
            }
            if (secondaryButtonLabel != null && onSecondaryClick != null) {
                OutlinedButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .let { if (secondaryTestTag != null) it.testTag(secondaryTestTag) else it }
                ) {
                    Text(text = secondaryButtonLabel)
                }
            }
        }
    }
}
