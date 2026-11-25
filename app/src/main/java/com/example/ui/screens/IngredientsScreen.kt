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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IngredientEntity
import com.example.domain.MeasurementUnit
import com.example.domain.PricingCalculator
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MoneyText
import kotlinx.coroutines.launch

val INGREDIENT_CATEGORIES = listOf("Todos", "Laticínios", "Chocolates", "Secos & Ovos", "Frutas", "Embalagens", "Outros")

@Composable
fun IngredientsScreen(
    ingredients: List<IngredientEntity>,
    onSaveIngredient: (Long, String, String, Double, String, Double) -> Unit,
    onDeleteIngredient: (Long) -> Unit,
    countUsages: suspend (Long) -> Int,
    onLoadStarterCatalog: () -> Unit,
    openCreateTrigger: Int,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var editingIngredient by remember { mutableStateOf<IngredientEntity?>(null) }
    var showFormDialog by remember(openCreateTrigger) { mutableStateOf(openCreateTrigger > 0) }
    var pendingDelete by remember { mutableStateOf<Pair<IngredientEntity, Int>?>(null) }
    val scope = rememberCoroutineScope()

    val filtered = remember(ingredients, searchQuery, selectedCategory) {
        ingredients.filter { item ->
            val matchesCat = selectedCategory == "Todos" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ingredients_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar insumo ou embalagem...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ingredients_search_input")
            )
        }

        item {
            CategoryFilterRow(
                categories = INGREDIENT_CATEGORIES,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it }
            )
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStatePanel(
                    title = if (ingredients.isEmpty()) "Seu estoque de insumos está vazio" else "Nenhum insumo encontrado",
                    subtitle = "Cadastre os preços pagos nas embalagens para o PrecificaDoce calcular o custo exato de cada grama ou unidade.",
                    primaryButtonLabel = "Cadastrar Insumo",
                    onPrimaryClick = {
                        editingIngredient = null
                        showFormDialog = true
                    },
                    primaryTestTag = "ingredients_empty_add_btn",
                    secondaryButtonLabel = if (ingredients.isEmpty()) "Carregar catálogo inicial" else null,
                    onSecondaryClick = if (ingredients.isEmpty()) onLoadStarterCatalog else null,
                    secondaryTestTag = "ingredients_empty_load_btn"
                )
            }
        } else {
            items(filtered, key = { it.id }) { ing ->
                val unit = MeasurementUnit.fromSymbol(ing.packageUnit)
                val unitCostText = PricingCalculator.calculatePackageUnitCostDisplay(
                    packageQuantity = ing.packageQuantity,
                    packageUnit = unit,
                    packagePrice = ing.packagePrice
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            editingIngredient = ing
                            showFormDialog = true
                        }
                        .testTag("ingredient_card_${ing.id}")
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
                                text = ing.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = ing.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Embalagem: ${PricingCalculator.formatQuantity(ing.packageQuantity)} ${ing.packageUnit}  •  $unitCostText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MoneyText(
                                amount = ing.packagePrice,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val count = countUsages(ing.id)
                                        pendingDelete = ing to count
                                    }
                                },
                                modifier = Modifier.testTag("delete_ingredient_${ing.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir insumo",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        IngredientFormDialog(
            initial = editingIngredient,
            onDismiss = {
                showFormDialog = false
                editingIngredient = null
            },
            onConfirm = { name, category, qty, unit, price ->
                onSaveIngredient(editingIngredient?.id ?: 0L, name, category, qty, unit, price)
                showFormDialog = false
                editingIngredient = null
            }
        )
    }

    pendingDelete?.let { (ing, usageCount) ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Excluir insumo?") },
            text = {
                Text(
                    if (usageCount > 0) {
                        "\"${ing.name}\" está sendo usado em $usageCount receita(s). Ao excluir, ele será removido dessas receitas."
                    } else {
                        "Deseja remover \"${ing.name}\" do seu catálogo?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteIngredient(ing.id)
                        pendingDelete = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun IngredientFormDialog(
    initial: IngredientEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Double) -> Unit
) {
    val editableCats = remember { INGREDIENT_CATEGORIES.filter { it != "Todos" } }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: editableCats.first()) }
    var qtyText by remember {
        mutableStateOf(initial?.packageQuantity?.let { PricingCalculator.formatQuantity(it) } ?: "395")
    }
    var selectedUnit by remember {
        mutableStateOf(MeasurementUnit.fromSymbol(initial?.packageUnit ?: "g"))
    }
    var priceText by remember { mutableStateOf(initial?.packagePrice?.toString() ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val parsedQty = qtyText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val parsedPrice = priceText.replace(',', '.').toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Novo Insumo" else "Editar Insumo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Nome do insumo") },
                    placeholder = { Text("Ex: Leite Condensado") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingredient_name_input")
                )

                Text("Categoria", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(editableCats) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qtd. embalagem") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ingredient_qty_input")
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Preço pago (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ingredient_price_input")
                    )
                }

                Text("Unidade da embalagem", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MeasurementUnit.entries) { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = unit },
                            label = { Text(unit.symbol) }
                        )
                    }
                }

                if (parsedQty > 0 && parsedPrice > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Custo proporcional: ${PricingCalculator.calculatePackageUnitCostDisplay(parsedQty, selectedUnit, parsedPrice)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Informe o nome do insumo."
                        return@Button
                    }
                    if (parsedQty <= 0.0 || parsedPrice <= 0.0) {
                        errorMsg = "Informe quantidade e preço maiores que zero."
                        return@Button
                    }
                    onConfirm(name.trim(), category, parsedQty, selectedUnit.symbol, parsedPrice)
                },
                modifier = Modifier.testTag("ingredient_save_btn")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
