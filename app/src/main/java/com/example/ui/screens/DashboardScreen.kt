package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.PricingCalculator
import com.example.ui.components.CostStackedBar
import com.example.ui.components.EmptyStatePanel
import com.example.ui.components.MoneyText
import com.example.ui.components.SectionHeader
import com.example.viewmodel.PrecificaUiState
import com.example.viewmodel.PricedRecipe

@Composable
fun DashboardScreen(
    uiState: PrecificaUiState,
    onNewRecipe: () -> Unit,
    onNewIngredient: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenRecipes: () -> Unit,
    onOpenRecipeDetail: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onLoadStarterCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editorial Patisserie Hero Banner
        item {
            HeroBannerCard(
                businessName = uiState.settings.businessName,
                hourlyRate = uiState.settings.hourlyRate,
                onOpenSettings = onOpenSettings,
                onNewRecipe = onNewRecipe
            )
        }

        // KPI Bento Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiBentoCard(
                    title = "Receitas",
                    value = uiState.recipes.size.toString(),
                    subtitle = "fichas técnicas",
                    icon = Icons.Default.RestaurantMenu,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_recipes")
                        .clickable { onOpenRecipes() }
                )
                KpiBentoCard(
                    title = "Insumos",
                    value = uiState.ingredients.size.toString(),
                    subtitle = "no catálogo",
                    icon = Icons.Default.Inventory2,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_ingredients")
                        .clickable { onNewIngredient() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiBentoCard(
                    title = "Margem Média",
                    value = PricingCalculator.formatPercent(uiState.averageMarginPercent),
                    subtitle = "sobre a venda",
                    icon = Icons.Outlined.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_margin")
                )
                KpiBentoCard(
                    title = "Orçamentos",
                    value = uiState.quotes.size.toString(),
                    subtitle = "salvos",
                    icon = Icons.Outlined.ReceiptLong,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_quotes")
                        .clickable { onOpenCalculator() }
                )
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onNewIngredient,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_new_ingredient")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Insumo", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                FilledTonalButton(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quick_calculator")
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Simular", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // Highlighted Recipes or Empty State
        item {
            SectionHeader(
                title = "Suas Receitas",
                actionLabel = if (uiState.recipes.isNotEmpty()) "Ver todas" else null,
                onActionClick = onOpenRecipes,
                actionTestTag = "see_all_recipes_btn"
            )
        }

        if (uiState.recipes.isEmpty()) {
            item {
                EmptyStatePanel(
                    title = "Comece a precificar seus doces",
                    subtitle = "Cadastre seus ingredientes e crie fichas técnicas com custo real, mão de obra e margem de lucro.",
                    primaryButtonLabel = "Criar primeira receita",
                    onPrimaryClick = onNewRecipe,
                    primaryTestTag = "empty_create_recipe_btn",
                    secondaryButtonLabel = if (uiState.ingredients.isEmpty()) "Carregar receitas de exemplo" else null,
                    onSecondaryClick = if (uiState.ingredients.isEmpty()) onLoadStarterCatalog else null,
                    secondaryTestTag = "empty_load_examples_btn"
                )
            }
        } else {
            items(uiState.recipes.take(4), key = { it.recipe.id }) { priced ->
                DashboardRecipeSummaryCard(
                    priced = priced,
                    onClick = { onOpenRecipeDetail(priced.recipe.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun HeroBannerCard(
    businessName: String,
    hourlyRate: Double,
    onOpenSettings: () -> Unit,
    onNewRecipe: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_patisserie_hero),
                contentDescription = stringResource(R.string.hero_banner_desc),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC231917),
                                Color(0x99231917),
                                Color(0xEB231917)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "PrecificaDoce",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações do negócio",
                            tint = Color.White
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Sua hora de produção",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFEBD9D2)
                            )
                            MoneyText(
                                amount = hourlyRate,
                                color = Color(0xFFFFD9E1),
                                fontSize = 18.sp
                            )
                        }
                        Button(
                            onClick = onNewRecipe,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("hero_new_recipe_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Nova Receita")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiBentoCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun DashboardRecipeSummaryCard(
    priced: PricedRecipe,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("dashboard_recipe_card_${priced.recipe.id}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = priced.recipe.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = priced.recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Venda / ${priced.breakdown.yieldUnit}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MoneyText(
                        amount = priced.breakdown.suggestedUnitPrice,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
            }

            CostStackedBar(breakdown = priced.breakdown)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custo: ${PricingCalculator.formatBrl(priced.breakdown.unitCost)}  •  Lucro: ${PricingCalculator.formatBrl(priced.breakdown.profitPerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Abrir ficha técnica",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
