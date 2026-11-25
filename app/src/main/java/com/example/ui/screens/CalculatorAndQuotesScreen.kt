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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OrderQuoteItemEntity
import com.example.data.preferences.BusinessSettings
import com.example.domain.PricingCalculator
import com.example.ui.components.CostDonutChart
import com.example.ui.components.MoneyText
import com.example.viewmodel.PricedQuote
import com.example.viewmodel.PricedRecipe
import kotlin.math.roundToInt

@Composable
fun CalculatorAndQuotesScreen(
    recipes: List<PricedRecipe>,
    quotes: List<PricedQuote>,
    settings: BusinessSettings,
    onSaveQuote: (String, String, Double, Double, String, List<OrderQuoteItemEntity>) -> Unit,
    onDeleteQuote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Calculadora Rápida, 1 = Orçamentos
    var showNewQuoteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_quotes_screen")
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("tab_quick_calc")
            ) {
                Text("Calculadora Rápida")
            }
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("tab_quotes")
            ) {
                Text("Orçamentos (${quotes.size})")
            }
        }

        if (selectedTab == 0) {
            QuickCalculatorContent(settings = settings)
        } else {
            QuotesListContent(
                quotes = quotes,
                hasRecipes = recipes.isNotEmpty(),
                onCreateNewQuote = { showNewQuoteDialog = true },
                onDeleteQuote = onDeleteQuote
            )
        }
    }

    if (showNewQuoteDialog) {
        CreateQuoteDialog(
            recipes = recipes,
            onDismiss = { showNewQuoteDialog = false },
            onSave = { customer, date, delivery, discount, notes, items ->
                onSaveQuote(customer, date, delivery, discount, notes, items)
                showNewQuoteDialog = false
            }
        )
    }
}

@Composable
private fun QuickCalculatorContent(settings: BusinessSettings) {
    var ingredientsCostText by remember { mutableStateOf("35.00") }
    var packagingCostText by remember { mutableStateOf("8.00") }
    var minutesText by remember { mutableStateOf("45") }
    var yieldText by remember { mutableStateOf("25") }
    var overheadPct by remember { mutableDoubleStateOf(settings.defaultOverheadPercent) }
    var markupPct by remember { mutableDoubleStateOf(settings.defaultProfitMarkupPercent) }
    var feePct by remember { mutableDoubleStateOf(settings.defaultCardFeePercent) }

    val breakdown = PricingCalculator.calculateBreakdown(
        ingredientsCost = ingredientsCostText.replace(',', '.').toDoubleOrNull() ?: 0.0,
        packagingCost = packagingCostText.replace(',', '.').toDoubleOrNull() ?: 0.0,
        prepTimeMinutes = minutesText.toIntOrNull() ?: 0,
        hourlyRate = settings.hourlyRate,
        overheadPercent = overheadPct,
        profitMarkupPercent = markupPct,
        cardFeePercent = feePct,
        yieldQuantity = yieldText.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0,
        yieldUnit = "un"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Simulação Instantânea",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Preço por unidade", style = MaterialTheme.typography.labelSmall)
                            MoneyText(
                                amount = breakdown.suggestedUnitPrice,
                                fontSize = 26.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Lucro líquido lote", style = MaterialTheme.typography.labelSmall)
                            MoneyText(
                                amount = breakdown.desiredProfit,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Text(
                        text = "Custo unitário: ${PricingCalculator.formatBrl(breakdown.unitCost)}  •  Margem real: ${PricingCalculator.formatPercent(breakdown.realMarginOnSalePercent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ingredientsCostText,
                            onValueChange = { ingredientsCostText = it },
                            label = { Text("Ingredientes (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_calc_ingredients_input")
                        )
                        OutlinedTextField(
                            value = packagingCostText,
                            onValueChange = { packagingCostText = it },
                            label = { Text("Embalagem (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = minutesText,
                            onValueChange = { minutesText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Tempo (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = yieldText,
                            onValueChange = { yieldText = it },
                            label = { Text("Rendimento (un)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    QuickSlider("Custos indiretos", overheadPct, 0f..50f) {
                        overheadPct = it.roundToInt().toDouble()
                    }
                    QuickSlider("Markup sobre custo", markupPct, 10f..250f) {
                        markupPct = it.roundToInt().toDouble()
                    }
                    QuickSlider("Taxa maquininha / app", feePct, 0f..30f) {
                        feePct = (it * 10).roundToInt() / 10.0
                    }
                }
            }
        }

        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CostDonutChart(breakdown = breakdown)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuickSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                PricingCalculator.formatPercent(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat().coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range
        )
    }
}

@Composable
private fun QuotesListContent(
    quotes: List<PricedQuote>,
    hasRecipes: Boolean,
    onCreateNewQuote: () -> Unit,
    onDeleteQuote: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onCreateNewQuote,
                enabled = hasRecipes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_quote_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (hasRecipes) "Novo Orçamento de Encomenda" else "Cadastre uma receita para gerar orçamentos")
            }
        }

        if (quotes.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum orçamento salvo. Use seus produtos cadastrados para montar pedidos com entrega, desconto e lucro líquido calculado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        } else {
            items(quotes, key = { it.quote.id }) { pq ->
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pq.quote.customerName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (pq.quote.eventDateText.isNotBlank()) {
                                    Text(
                                        text = "Entrega: ${pq.quote.eventDateText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteQuote(pq.quote.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir orçamento")
                            }
                        }

                        pq.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.itemName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                MoneyText(item.unitPrice * item.quantity, fontSize = 13.sp)
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Cliente", style = MaterialTheme.typography.labelSmall)
                                MoneyText(pq.totalPrice, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Lucro Líquido", style = MaterialTheme.typography.labelSmall)
                                MoneyText(pq.netProfit, fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateQuoteDialog(
    recipes: List<PricedRecipe>,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, String, List<OrderQuoteItemEntity>) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var deliveryFeeText by remember { mutableStateOf("0") }
    var discountText by remember { mutableStateOf("0") }

    val selectedQuantities = remember {
        mutableStateListOf<Pair<PricedRecipe, Int>>().apply {
            recipes.firstOrNull()?.let { add(it to it.breakdown.yieldQuantity.toInt().coerceAtLeast(1)) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Orçamento") },
        text = {
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nome do cliente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Data da entrega (opcional)") },
                        placeholder = { Text("Ex: Sábado 14h") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deliveryFeeText,
                            onValueChange = { deliveryFeeText = it },
                            label = { Text("Entrega (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Desconto (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("Itens da encomenda:", style = MaterialTheme.typography.labelLarge)
                }
                items(recipes, key = { it.recipe.id }) { priced ->
                    val currentPair = selectedQuantities.firstOrNull { it.first.recipe.id == priced.recipe.id }
                    val qty = currentPair?.second ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(priced.recipe.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "${PricingCalculator.formatBrl(priced.breakdown.suggestedUnitPrice)} / ${priced.breakdown.yieldUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val idx = selectedQuantities.indexOfFirst { it.first.recipe.id == priced.recipe.id }
                                    if (idx >= 0) {
                                        val next = (selectedQuantities[idx].second - 5).coerceAtLeast(0)
                                        if (next == 0) selectedQuantities.removeAt(idx) else selectedQuantities[idx] = priced to next
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                            }
                            Text(qty.toString(), style = MaterialTheme.typography.titleSmall)
                            IconButton(
                                onClick = {
                                    val idx = selectedQuantities.indexOfFirst { it.first.recipe.id == priced.recipe.id }
                                    if (idx >= 0) {
                                        selectedQuantities[idx] = priced to (selectedQuantities[idx].second + 5)
                                    } else {
                                        selectedQuantities.add(priced to 10)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val items = selectedQuantities.filter { it.second > 0 }.map { (priced, qty) ->
                        OrderQuoteItemEntity(
                            quoteId = 0,
                            recipeId = priced.recipe.id,
                            itemName = priced.recipe.name,
                            quantity = qty,
                            unitCost = priced.breakdown.unitCost,
                            unitPrice = priced.breakdown.suggestedUnitPrice
                        )
                    }
                    if (items.isNotEmpty()) {
                        onSave(
                            customerName,
                            eventDate,
                            deliveryFeeText.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            discountText.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            "",
                            items
                        )
                    }
                }
            ) {
                Text("Salvar Orçamento")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
