package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IngredientEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.RecipeIngredientEntity
import com.example.data.preferences.BusinessSettings
import com.example.domain.MeasurementUnit
import com.example.domain.PricingCalculator
import com.example.ui.components.MoneyText
import com.example.viewmodel.PricedRecipe
import kotlin.math.roundToInt

private data class DraftIngredientLine(
    val ingredient: IngredientEntity,
    val quantityUsed: Double,
    val unitUsed: MeasurementUnit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    existingRecipe: PricedRecipe?,
    allIngredients: List<IngredientEntity>,
    settings: BusinessSettings,
    onSave: (RecipeEntity, List<RecipeIngredientEntity>) -> Unit,
    onQuickAddIngredient: (String, String, Double, String, Double) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editableCategories = remember { RECIPE_CATEGORIES.filter { it != "Todos" } }

    var name by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.name ?: "")
    }
    var category by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.category ?: editableCategories.first())
    }
    var yieldQuantityText by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.yieldQuantity?.let { PricingCalculator.formatQuantity(it) } ?: "30")
    }
    var yieldUnit by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.yieldUnit ?: "unidades")
    }
    var prepMinutesText by remember(existingRecipe?.recipe?.id) {
        mutableStateOf((existingRecipe?.recipe?.prepTimeMinutes ?: 45).toString())
    }
    var packagingCostText by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.packagingCost?.toString() ?: "0")
    }
    var overheadPercent by remember(existingRecipe?.recipe?.id) {
        mutableDoubleStateOf(existingRecipe?.recipe?.overheadPercent ?: settings.defaultOverheadPercent)
    }
    var profitMarkupPercent by remember(existingRecipe?.recipe?.id) {
        mutableDoubleStateOf(existingRecipe?.recipe?.profitMarkupPercent ?: settings.defaultProfitMarkupPercent)
    }
    var cardFeePercent by remember(existingRecipe?.recipe?.id) {
        mutableDoubleStateOf(existingRecipe?.recipe?.cardFeePercent ?: settings.defaultCardFeePercent)
    }
    var notes by remember(existingRecipe?.recipe?.id) {
        mutableStateOf(existingRecipe?.recipe?.notes ?: "")
    }

    val draftLines = remember(existingRecipe?.recipe?.id) {
        mutableStateListOf<DraftIngredientLine>().apply {
            existingRecipe?.lines?.forEach { line ->
                val entity = allIngredients.firstOrNull { it.id == line.ingredientId }
                if (entity != null) {
                    add(DraftIngredientLine(entity, line.quantityUsed, line.unitUsed))
                }
            }
        }
    }

    var showIngredientPicker by remember { mutableStateOf(false) }
    var showQuickIngredientDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val parsedYield = yieldQuantityText.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val parsedMinutes = prepMinutesText.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val parsedPackaging = packagingCostText.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

    val ingredientsCost = draftLines.sumOf { line ->
        PricingCalculator.calculateIngredientLineCost(
            quantityUsed = line.quantityUsed,
            unitUsed = line.unitUsed,
            packageQuantity = line.ingredient.packageQuantity,
            packageUnit = MeasurementUnit.fromSymbol(line.ingredient.packageUnit),
            packagePrice = line.ingredient.packagePrice
        )
    }

    val liveBreakdown = PricingCalculator.calculateBreakdown(
        ingredientsCost = ingredientsCost,
        packagingCost = parsedPackaging,
        prepTimeMinutes = parsedMinutes,
        hourlyRate = settings.hourlyRate,
        overheadPercent = overheadPercent,
        profitMarkupPercent = profitMarkupPercent,
        cardFeePercent = cardFeePercent,
        yieldQuantity = parsedYield,
        yieldUnit = yieldUnit
    )

    Scaffold(
        modifier = modifier.testTag("recipe_editor_screen"),
        topBar = {
            TopAppBar(
                title = { Text(if (existingRecipe == null) "Nova Receita" else "Editar Receita") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) {
                                validationError = "Informe o nome da receita."
                                return@TextButton
                            }
                            onSave(
                                RecipeEntity(
                                    id = existingRecipe?.recipe?.id ?: 0L,
                                    name = name.trim(),
                                    category = category,
                                    yieldQuantity = parsedYield,
                                    yieldUnit = yieldUnit.trim().ifBlank { "un" },
                                    prepTimeMinutes = parsedMinutes,
                                    packagingCost = parsedPackaging,
                                    overheadPercent = overheadPercent,
                                    profitMarkupPercent = profitMarkupPercent,
                                    cardFeePercent = cardFeePercent,
                                    notes = notes.trim()
                                ),
                                draftLines.map { line ->
                                    RecipeIngredientEntity(
                                        recipeId = existingRecipe?.recipe?.id ?: 0L,
                                        ingredientId = line.ingredient.id,
                                        quantityUsed = line.quantityUsed,
                                        unitUsed = line.unitUsed.symbol
                                    )
                                }
                            )
                        },
                        modifier = Modifier.testTag("editor_save_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Salvar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Custo: ${PricingCalculator.formatBrl(liveBreakdown.unitCost)} / ${liveBreakdown.yieldUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Lucro: ${PricingCalculator.formatBrl(liveBreakdown.desiredProfit)} (${PricingCalculator.formatPercent(liveBreakdown.realMarginOnSalePercent)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sugerido / ${liveBreakdown.yieldUnit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        MoneyText(
                            amount = liveBreakdown.suggestedUnitPrice,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (validationError != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Nome do doce / receita") },
                    placeholder = { Text("Ex: Brigadeiro Belga 50%") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_name_input")
                )
            }

            item {
                Text("Categoria", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(editableCategories) { cat ->
                        FilterChip(
                            selected = cat == category,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = yieldQuantityText,
                        onValueChange = { yieldQuantityText = it },
                        label = { Text("Rendimento") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipe_yield_input")
                    )
                    OutlinedTextField(
                        value = yieldUnit,
                        onValueChange = { yieldUnit = it },
                        label = { Text("Unidade") },
                        placeholder = { Text("unidades, fatias") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipe_yield_unit_input")
                    )
                }
            }

            // Ingredients Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredientes (${draftLines.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { showQuickIngredientDialog = true },
                            modifier = Modifier.testTag("editor_quick_add_ingredient_btn")
                        ) {
                            Text("+ Criar Insumo")
                        }
                        Button(
                            onClick = { showIngredientPicker = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("editor_pick_ingredient_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Adicionar")
                        }
                    }
                }
            }

            if (draftLines.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nenhum ingrediente adicionado ainda. Toque em \"Adicionar\" para selecionar do seu catálogo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(draftLines.indices.toList(), key = { idx -> "${draftLines[idx].ingredient.id}_$idx" }) { index ->
                    val line = draftLines[index]
                    val cost = PricingCalculator.calculateIngredientLineCost(
                        quantityUsed = line.quantityUsed,
                        unitUsed = line.unitUsed,
                        packageQuantity = line.ingredient.packageQuantity,
                        packageUnit = MeasurementUnit.fromSymbol(line.ingredient.packageUnit),
                        packagePrice = line.ingredient.packagePrice
                    )
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = line.ingredient.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${PricingCalculator.formatQuantity(line.quantityUsed)} ${line.unitUsed.symbol}  •  ${PricingCalculator.formatBrl(cost)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { draftLines.removeAt(index) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover ingrediente")
                            }
                        }
                    }
                }
            }

            // Production & Packaging
            item {
                Text("Produção e Embalagem", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prepMinutesText,
                        onValueChange = { prepMinutesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Tempo (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipe_minutes_input")
                    )
                    OutlinedTextField(
                        value = packagingCostText,
                        onValueChange = { packagingCostText = it },
                        label = { Text("Embalagem (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipe_packaging_input")
                    )
                }
            }

            // Margin & Fee Sliders
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Margens e Taxas", style = MaterialTheme.typography.titleMedium)

                        SliderControl(
                            label = "Custos indiretos (gás, luz, água)",
                            value = overheadPercent,
                            valueRange = 0f..50f,
                            onValueChange = { overheadPercent = it.roundToInt().toDouble() }
                        )
                        SliderControl(
                            label = "Markup / Lucro sobre custo",
                            value = profitMarkupPercent,
                            valueRange = 10f..250f,
                            onValueChange = { profitMarkupPercent = it.roundToInt().toDouble() }
                        )
                        SliderControl(
                            label = "Taxa maquininha / delivery",
                            value = cardFeePercent,
                            valueRange = 0f..30f,
                            onValueChange = { cardFeePercent = (it * 10).roundToInt() / 10.0 }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações da ficha técnica") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showIngredientPicker) {
        PickIngredientDialog(
            allIngredients = allIngredients,
            onDismiss = { showIngredientPicker = false },
            onSelect = { ing, qty, unit ->
                draftLines.add(DraftIngredientLine(ing, qty, unit))
                showIngredientPicker = false
            },
            onOpenCreateNew = {
                showIngredientPicker = false
                showQuickIngredientDialog = true
            }
        )
    }

    if (showQuickIngredientDialog) {
        IngredientFormDialog(
            initial = null,
            onDismiss = { showQuickIngredientDialog = false },
            onConfirm = { ingName, ingCat, packQty, packUnit, packPrice ->
                onQuickAddIngredient(ingName, ingCat, packQty, packUnit, packPrice)
                showQuickIngredientDialog = false
            }
        )
    }
}

@Composable
private fun SliderControl(
    label: String,
    value: Double,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = PricingCalculator.formatPercent(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun PickIngredientDialog(
    allIngredients: List<IngredientEntity>,
    onDismiss: () -> Unit,
    onSelect: (IngredientEntity, Double, MeasurementUnit) -> Unit,
    onOpenCreateNew: () -> Unit
) {
    var selectedIngredient by remember { mutableStateOf(allIngredients.firstOrNull()) }
    var qtyText by remember { mutableStateOf("100") }
    var selectedUnit by remember(selectedIngredient) {
        mutableStateOf(
            selectedIngredient?.let { MeasurementUnit.fromSymbol(it.packageUnit) }
                ?: MeasurementUnit.GRAMS
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Ingrediente") },
        text = {
            if (allIngredients.isEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Você ainda não tem insumos cadastrados no catálogo.")
                    OutlinedButton(onClick = onOpenCreateNew, modifier = Modifier.fillMaxWidth()) {
                        Text("Cadastrar primeiro insumo")
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Selecione o insumo:", style = MaterialTheme.typography.labelLarge)
                    LazyColumn(modifier = Modifier.height(150.dp)) {
                        items(allIngredients, key = { it.id }) { ing ->
                            val isSelected = selectedIngredient?.id == ing.id
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedIngredient = ing
                                        val packUnit = MeasurementUnit.fromSymbol(ing.packageUnit)
                                        selectedUnit = when (packUnit) {
                                            MeasurementUnit.KILOGRAMS -> MeasurementUnit.GRAMS
                                            MeasurementUnit.LITERS -> MeasurementUnit.MILLILITERS
                                            else -> packUnit
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(ing.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    Text(
                                        PricingCalculator.formatBrl(ing.packagePrice),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Quantidade usada") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val compatibleUnits = selectedIngredient?.let {
                        MeasurementUnit.fromSymbol(it.packageUnit).compatibleUnits()
                    } ?: listOf(MeasurementUnit.GRAMS)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        compatibleUnits.forEach { u ->
                            FilterChip(
                                selected = selectedUnit == u,
                                onClick = { selectedUnit = u },
                                label = { Text(u.symbol) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedIngredient != null) {
                Button(
                    onClick = {
                        val qty = qtyText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (qty > 0.0) {
                            onSelect(selectedIngredient!!, qty, selectedUnit)
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Fechar")
            }
        }
    )
}
