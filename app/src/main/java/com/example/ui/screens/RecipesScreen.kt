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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PricingCalculator
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.CostStackedBar
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MoneyText
import com.example.viewmodel.PricedRecipe

val RECIPE_CATEGORIES = listOf("Todos", "Brigadeiros", "Bolos", "Brownies", "Tortas", "Sobremesas")

@Composable
fun RecipesScreen(
    recipes: List<PricedRecipe>,
    hasIngredients: Boolean,
    onCreateRecipe: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onLoadStarterCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val filtered = remember(recipes, searchQuery, selectedCategory) {
        recipes.filter { item ->
            val matchesCategory = selectedCategory == "Todos" ||
                item.recipe.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                item.recipe.name.contains(searchQuery, ignoreCase = true) ||
                item.recipe.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recipes_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar receita ou categoria...") },
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
                    .testTag("recipes_search_input")
            )
        }

        item {
            CategoryFilterRow(
                categories = RECIPE_CATEGORIES,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it }
            )
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStatePanel(
                    title = if (recipes.isEmpty()) "Nenhuma receita cadastrada" else "Nenhuma receita encontrada",
                    subtitle = if (recipes.isEmpty()) {
                        "Monte sua primeira ficha técnica para calcular automaticamente preço de venda e lucro líquido."
                    } else {
                        "Tente buscar outro nome ou limpar os filtros selecionados."
                    },
                    primaryButtonLabel = "Nova Receita",
                    onPrimaryClick = onCreateRecipe,
                    primaryTestTag = "recipes_empty_new_btn",
                    secondaryButtonLabel = if (recipes.isEmpty() && !hasIngredients) "Carregar receitas de exemplo" else null,
                    onSecondaryClick = if (recipes.isEmpty() && !hasIngredients) onLoadStarterCatalog else null,
                    secondaryTestTag = "recipes_empty_load_examples_btn"
                )
            }
        } else {
            items(filtered, key = { it.recipe.id }) { priced ->
                RecipeListCard(
                    priced = priced,
                    onClick = { onOpenDetail(priced.recipe.id) }
                )
            }
        }
    }
}

@Composable
private fun RecipeListCard(
    priced: PricedRecipe,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("recipe_card_${priced.recipe.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = priced.recipe.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = priced.recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Rendimento: ${PricingCalculator.formatQuantity(priced.breakdown.yieldQuantity)} ${priced.breakdown.yieldUnit}  •  ${priced.recipe.prepTimeMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Preço Sugerido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        amount = priced.breakdown.suggestedUnitPrice,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "por ${priced.breakdown.yieldUnit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CostStackedBar(breakdown = priced.breakdown, height = 8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Custo unitário",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        amount = priced.breakdown.unitCost,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Venda lote",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        amount = priced.breakdown.finalSellingTotal,
                        fontSize = 14.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Lucro (${PricingCalculator.formatPercent(priced.breakdown.realMarginOnSalePercent)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    MoneyText(
                        amount = priced.breakdown.desiredProfit,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
