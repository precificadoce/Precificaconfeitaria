package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderQuoteItemEntity
import com.example.domain.PricingCalculator
import com.example.ui.components.CostDonutChart
import com.example.ui.components.MoneyText
import com.example.viewmodel.PricedRecipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    pricedRecipe: PricedRecipe?,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onCreateQuoteFromRecipe: (String, OrderQuoteItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (pricedRecipe == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ficha Técnica") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Receita não encontrada.", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    val recipe = pricedRecipe.recipe
    val b = pricedRecipe.breakdown
    var simulatedQty by remember(recipe.id) {
        mutableIntStateOf(b.yieldQuantity.toInt().coerceAtLeast(1))
    }

    Scaffold(
        modifier = modifier.testTag("recipe_detail_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Ficha Técnica", maxLines = 1) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEdit(recipe.id) },
                        modifier = Modifier.testTag("detail_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar receita")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir receita")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Summary Card
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = recipe.category.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Preço Sugerido / ${b.yieldUnit}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                                MoneyText(
                                    amount = b.suggestedUnitPrice,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Lucro / ${b.yieldUnit}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                MoneyText(
                                    amount = b.profitPerUnit,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            // Donut Breakdown
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Composição do Preço (Lote: ${PricingCalculator.formatQuantity(b.yieldQuantity)} ${b.yieldUnit})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        CostDonutChart(breakdown = b)
                        HorizontalDivider()
                        BreakdownLine("Ingredientes", b.ingredientsCost)
                        BreakdownLine("Custos Indiretos (${PricingCalculator.formatPercent(recipe.overheadPercent)})", b.overheadCost)
                        BreakdownLine("Mão de Obra (${recipe.prepTimeMinutes} min)", b.laborCost)
                        BreakdownLine("Embalagem do Lote", b.packagingCost)
                        BreakdownLine(
                            label = "Custo Total de Produção",
                            amount = b.totalBatchCost,
                            isBold = true
                        )
                        BreakdownLine(
                            label = "Lucro Líquido (Markup ${PricingCalculator.formatPercent(recipe.profitMarkupPercent)})",
                            amount = b.desiredProfit,
                            valueColor = MaterialTheme.colorScheme.tertiary,
                            isBold = true
                        )
                        if (b.cardFeeCost > 0.0) {
                            BreakdownLine(
                                label = "Taxa Maquininha/App (${PricingCalculator.formatPercent(recipe.cardFeePercent)})",
                                amount = b.cardFeeCost
                            )
                        }
                        BreakdownLine(
                            label = "Preço Final de Venda (Lote)",
                            amount = b.finalSellingTotal,
                            valueColor = MaterialTheme.colorScheme.primary,
                            isBold = true
                        )
                    }
                }
            }

            // Order Quantity Simulator
            item {
                val simCost = b.unitCost * simulatedQty
                val simPrice = b.suggestedUnitPrice * simulatedQty
                val simProfit = b.profitPerUnit * simulatedQty

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Simulador de Encomenda",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quantidade (${b.yieldUnit}):",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (simulatedQty > 1) simulatedQty -= 1 },
                                    modifier = Modifier.testTag("sim_minus_btn")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Diminuir quantidade")
                                }
                                Text(
                                    text = simulatedQty.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                IconButton(
                                    onClick = { simulatedQty += 1 },
                                    modifier = Modifier.testTag("sim_plus_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar quantidade")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Custo", style = MaterialTheme.typography.labelSmall)
                                MoneyText(simCost, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Venda Total", style = MaterialTheme.typography.labelSmall)
                                MoneyText(simPrice, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Lucro", style = MaterialTheme.typography.labelSmall)
                                MoneyText(simProfit, fontSize = 15.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        Button(
                            onClick = {
                                onCreateQuoteFromRecipe(
                                    recipe.name,
                                    OrderQuoteItemEntity(
                                        quoteId = 0,
                                        recipeId = recipe.id,
                                        itemName = recipe.name,
                                        quantity = simulatedQty,
                                        unitCost = b.unitCost,
                                        unitPrice = b.suggestedUnitPrice
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_quote_from_recipe_btn")
                        ) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar como Orçamento")
                        }
                    }
                }
            }

            // Ingredients used in recipe
            item {
                Text(
                    text = "Ingredientes da Receita (${pricedRecipe.lines.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(pricedRecipe.lines, key = { it.ingredientId }) { line ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = line.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Usa ${PricingCalculator.formatQuantity(line.quantityUsed)} ${line.unitUsed.symbol}  •  Emb: ${PricingCalculator.formatQuantity(line.packageQuantity)} ${line.packageUnit.symbol} (${PricingCalculator.formatBrl(line.packagePrice)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        MoneyText(amount = line.lineCost, fontSize = 15.sp)
                    }
                }
            }

            if (recipe.notes.isNotBlank()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Observações", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(recipe.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir receita?") },
            text = { Text("Tem certeza que deseja excluir \"${recipe.name}\"? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(recipe.id)
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BreakdownLine(
    label: String,
    amount: Double,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        MoneyText(
            amount = amount,
            color = valueColor,
            fontSize = if (isBold) 16.sp else 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}
