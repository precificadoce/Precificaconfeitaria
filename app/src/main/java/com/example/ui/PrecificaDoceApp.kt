package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.CalculatorAndQuotesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IngredientsScreen
import com.example.ui.screens.RecipeDetailScreen
import com.example.ui.screens.RecipeEditorScreen
import com.example.ui.screens.RecipesScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.PrecificaViewModel

object NavRoutes {
    const val HOME = "home"
    const val RECIPES = "recipes"
    const val INGREDIENTS = "ingredients"
    const val CALCULATOR = "calculator"
    const val SETTINGS = "settings"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_EDITOR = "recipe_editor/{recipeId}"

    fun recipeDetail(recipeId: Long) = "recipe_detail/$recipeId"
    fun recipeEditor(recipeId: Long = 0L) = "recipe_editor/$recipeId"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val TOP_DESTINATIONS = listOf(
    TopLevelDestination(NavRoutes.HOME, "Início", Icons.Filled.Home, Icons.Outlined.Home),
    TopLevelDestination(NavRoutes.RECIPES, "Receitas", Icons.Filled.RestaurantMenu, Icons.Outlined.RestaurantMenu),
    TopLevelDestination(NavRoutes.INGREDIENTS, "Insumos", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    TopLevelDestination(NavRoutes.CALCULATOR, "Calculadora", Icons.Filled.Calculate, Icons.Outlined.Calculate)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecificaDoceApp(
    viewModel: PrecificaViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var openIngredientDialogTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(feedback) {
        feedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showTopLevelChrome = currentRoute in listOf(
        NavRoutes.HOME,
        NavRoutes.RECIPES,
        NavRoutes.INGREDIENTS,
        NavRoutes.CALCULATOR
    )

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (showTopLevelChrome && currentRoute != NavRoutes.HOME) {
                    val title = when (currentRoute) {
                        NavRoutes.RECIPES -> "Receitas & Precificação"
                        NavRoutes.INGREDIENTS -> "Catálogo de Insumos"
                        NavRoutes.CALCULATOR -> "Calculadora & Orçamentos"
                        else -> "PrecificaDoce"
                    }
                    TopAppBar(
                        title = { Text(title) },
                        actions = {
                            IconButton(
                                onClick = { navController.navigate(NavRoutes.SETTINGS) },
                                modifier = Modifier.testTag("topbar_settings_btn")
                            ) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showTopLevelChrome && !useNavigationRail) {
                    NavigationBar {
                        TOP_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("bottom_nav_${dest.route}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                when (currentRoute) {
                    NavRoutes.RECIPES -> {
                        FloatingActionButton(
                            onClick = { navController.navigate(NavRoutes.recipeEditor(0L)) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_new_recipe")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova receita")
                        }
                    }
                    NavRoutes.INGREDIENTS -> {
                        FloatingActionButton(
                            onClick = { openIngredientDialogTrigger += 1 },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("fab_new_ingredient")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Novo insumo")
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelChrome && useNavigationRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        TOP_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_nav_${dest.route}")
                            )
                        }
                        NavigationRailItem(
                            selected = currentRoute == NavRoutes.SETTINGS,
                            onClick = { navController.navigate(NavRoutes.SETTINGS) },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") },
                            label = { Text("Ajustes") }
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                    ) {
                        composable(NavRoutes.HOME) {
                            DashboardScreen(
                                uiState = uiState,
                                onNewRecipe = { navController.navigate(NavRoutes.recipeEditor(0L)) },
                                onNewIngredient = {
                                    navigateTopLevel(NavRoutes.INGREDIENTS)
                                    openIngredientDialogTrigger += 1
                                },
                                onOpenCalculator = { navigateTopLevel(NavRoutes.CALCULATOR) },
                                onOpenRecipes = { navigateTopLevel(NavRoutes.RECIPES) },
                                onOpenRecipeDetail = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) },
                                onLoadStarterCatalog = { viewModel.loadStarterCatalog() }
                            )
                        }

                        composable(NavRoutes.RECIPES) {
                            RecipesScreen(
                                recipes = uiState.recipes,
                                hasIngredients = uiState.ingredients.isNotEmpty(),
                                onCreateRecipe = { navController.navigate(NavRoutes.recipeEditor(0L)) },
                                onOpenDetail = { id -> navController.navigate(NavRoutes.recipeDetail(id)) },
                                onLoadStarterCatalog = { viewModel.loadStarterCatalog() }
                            )
                        }

                        composable(NavRoutes.INGREDIENTS) {
                            IngredientsScreen(
                                ingredients = uiState.ingredients,
                                onSaveIngredient = { id, name, cat, qty, unit, price ->
                                    viewModel.saveIngredient(id, name, cat, qty, unit, price)
                                },
                                onDeleteIngredient = { viewModel.deleteIngredient(it) },
                                countUsages = { viewModel.countIngredientUsages(it) },
                                onLoadStarterCatalog = { viewModel.loadStarterCatalog() },
                                openCreateTrigger = openIngredientDialogTrigger
                            )
                        }

                        composable(NavRoutes.CALCULATOR) {
                            CalculatorAndQuotesScreen(
                                recipes = uiState.recipes,
                                quotes = uiState.quotes,
                                settings = uiState.settings,
                                onSaveQuote = { customer, date, delivery, discount, notes, items ->
                                    viewModel.saveQuote(customer, date, delivery, discount, notes, items)
                                },
                                onDeleteQuote = { viewModel.deleteQuote(it) }
                            )
                        }

                        composable(NavRoutes.SETTINGS) {
                            SettingsScreen(
                                settings = uiState.settings,
                                onSaveSettings = { viewModel.updateSettings(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = NavRoutes.RECIPE_DETAIL,
                            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                            val priced = uiState.recipes.firstOrNull { it.recipe.id == recipeId }
                            RecipeDetailScreen(
                                pricedRecipe = priced,
                                onBack = { navController.popBackStack() },
                                onEdit = { id -> navController.navigate(NavRoutes.recipeEditor(id)) },
                                onDelete = { id ->
                                    viewModel.deleteRecipe(id) {
                                        navController.popBackStack()
                                    }
                                },
                                onCreateQuoteFromRecipe = { recipeName, item ->
                                    viewModel.saveQuote(
                                        customerName = "Encomenda • $recipeName",
                                        eventDateText = "",
                                        deliveryFee = 0.0,
                                        discountAmount = 0.0,
                                        notes = "",
                                        items = listOf(item)
                                    )
                                    navigateTopLevel(NavRoutes.CALCULATOR)
                                }
                            )
                        }

                        composable(
                            route = NavRoutes.RECIPE_EDITOR,
                            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                            val existing = if (recipeId > 0L) {
                                uiState.recipes.firstOrNull { it.recipe.id == recipeId }
                            } else {
                                null
                            }
                            RecipeEditorScreen(
                                existingRecipe = existing,
                                allIngredients = uiState.ingredients,
                                settings = uiState.settings,
                                onSave = { recipe, items ->
                                    viewModel.saveRecipe(recipe, items) {
                                        navController.popBackStack()
                                    }
                                },
                                onQuickAddIngredient = { name, cat, qty, unit, price ->
                                    viewModel.saveIngredient(0L, name, cat, qty, unit, price)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
