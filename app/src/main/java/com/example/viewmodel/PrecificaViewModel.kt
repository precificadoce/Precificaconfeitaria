package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.IngredientEntity
import com.example.data.local.OrderQuoteEntity
import com.example.data.local.OrderQuoteItemEntity
import com.example.data.local.PrecificaDoceDatabase
import com.example.data.local.QuoteWithItems
import com.example.data.local.RecipeEntity
import com.example.data.local.RecipeIngredientEntity
import com.example.data.local.RecipeWithIngredients
import com.example.data.preferences.BusinessSettings
import com.example.data.preferences.BusinessSettingsRepository
import com.example.data.repository.PrecificaRepository
import com.example.domain.MeasurementUnit
import com.example.domain.PricedIngredientLine
import com.example.domain.PricingCalculator
import com.example.domain.RecipePricingBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PricedRecipe(
    val recipe: RecipeEntity,
    val lines: List<PricedIngredientLine>,
    val breakdown: RecipePricingBreakdown
)

data class PricedQuote(
    val quote: OrderQuoteEntity,
    val items: List<OrderQuoteItemEntity>,
    val subtotalPrice: Double,
    val subtotalCost: Double,
    val totalPrice: Double,
    val totalCost: Double,
    val netProfit: Double
)

data class PrecificaUiState(
    val isLoading: Boolean = true,
    val recipes: List<PricedRecipe> = emptyList(),
    val ingredients: List<IngredientEntity> = emptyList(),
    val quotes: List<PricedQuote> = emptyList(),
    val settings: BusinessSettings = BusinessSettings(),
    val averageMarginPercent: Double = 0.0
)

class PrecificaViewModel(
    private val repository: PrecificaRepository,
    private val settingsRepository: BusinessSettingsRepository
) : ViewModel() {

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val uiState: StateFlow<PrecificaUiState> = combine(
        repository.recipes,
        repository.ingredients,
        repository.quotes,
        settingsRepository.settingsFlow
    ) { recipeList, ingredientList, quoteList, settings ->
        val pricedRecipes = recipeList.map { rel -> mapToPricedRecipe(rel, settings) }
        val pricedQuotes = quoteList.map { mapToPricedQuote(it) }
        val avgMargin = if (pricedRecipes.isNotEmpty()) {
            pricedRecipes.map { it.breakdown.realMarginOnSalePercent }.average()
        } else {
            0.0
        }

        PrecificaUiState(
            isLoading = false,
            recipes = pricedRecipes,
            ingredients = ingredientList,
            quotes = pricedQuotes,
            settings = settings,
            averageMarginPercent = avgMargin
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrecificaUiState(isLoading = true)
    )

    private fun mapToPricedRecipe(
        rel: RecipeWithIngredients,
        settings: BusinessSettings
    ): PricedRecipe {
        val lines = rel.items.map { itemWithIngredient ->
            val ing = itemWithIngredient.ingredient
            val used = itemWithIngredient.recipeIngredient
            val unitUsed = MeasurementUnit.fromSymbol(used.unitUsed)
            val packUnit = MeasurementUnit.fromSymbol(ing.packageUnit)
            val cost = PricingCalculator.calculateIngredientLineCost(
                quantityUsed = used.quantityUsed,
                unitUsed = unitUsed,
                packageQuantity = ing.packageQuantity,
                packageUnit = packUnit,
                packagePrice = ing.packagePrice
            )
            PricedIngredientLine(
                ingredientId = ing.id,
                name = ing.name,
                quantityUsed = used.quantityUsed,
                unitUsed = unitUsed,
                packageQuantity = ing.packageQuantity,
                packageUnit = packUnit,
                packagePrice = ing.packagePrice,
                lineCost = cost
            )
        }
        val ingredientsSum = lines.sumOf { it.lineCost }
        val breakdown = PricingCalculator.calculateBreakdown(
            ingredientsCost = ingredientsSum,
            packagingCost = rel.recipe.packagingCost,
            prepTimeMinutes = rel.recipe.prepTimeMinutes,
            hourlyRate = settings.hourlyRate,
            overheadPercent = rel.recipe.overheadPercent,
            profitMarkupPercent = rel.recipe.profitMarkupPercent,
            cardFeePercent = rel.recipe.cardFeePercent,
            yieldQuantity = rel.recipe.yieldQuantity,
            yieldUnit = rel.recipe.yieldUnit
        )
        return PricedRecipe(
            recipe = rel.recipe,
            lines = lines,
            breakdown = breakdown
        )
    }

    private fun mapToPricedQuote(rel: QuoteWithItems): PricedQuote {
        val subtotalPrice = rel.items.sumOf { it.unitPrice * it.quantity }
        val subtotalCost = rel.items.sumOf { it.unitCost * it.quantity }
        val totalPrice = (subtotalPrice + rel.quote.deliveryFee - rel.quote.discountAmount).coerceAtLeast(0.0)
        val totalCost = subtotalCost + rel.quote.deliveryFee
        val netProfit = totalPrice - totalCost
        return PricedQuote(
            quote = rel.quote,
            items = rel.items,
            subtotalPrice = subtotalPrice,
            subtotalCost = subtotalCost,
            totalPrice = totalPrice,
            totalCost = totalCost,
            netProfit = netProfit
        )
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun saveIngredient(
        id: Long = 0L,
        name: String,
        category: String,
        packageQuantity: Double,
        packageUnit: String,
        packagePrice: Double
    ) {
        viewModelScope.launch {
            repository.saveIngredient(
                IngredientEntity(
                    id = id,
                    name = name.trim(),
                    category = category,
                    packageQuantity = packageQuantity,
                    packageUnit = packageUnit,
                    packagePrice = packagePrice
                )
            )
            _feedbackMessage.value = if (id == 0L) "Insumo cadastrado!" else "Insumo atualizado!"
        }
    }

    suspend fun countIngredientUsages(ingredientId: Long): Int =
        repository.countIngredientUsages(ingredientId)

    fun deleteIngredient(ingredientId: Long) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredientId)
            _feedbackMessage.value = "Insumo removido."
        }
    }

    fun saveRecipe(
        recipe: RecipeEntity,
        items: List<RecipeIngredientEntity>,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val savedId = repository.saveRecipe(recipe, items)
            _feedbackMessage.value = if (recipe.id == 0L) "Receita salva!" else "Receita atualizada!"
            onSaved(savedId)
        }
    }

    fun deleteRecipe(recipeId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
            _feedbackMessage.value = "Receita excluída."
            onDeleted()
        }
    }

    fun saveQuote(
        customerName: String,
        eventDateText: String,
        deliveryFee: Double,
        discountAmount: Double,
        notes: String,
        items: List<OrderQuoteItemEntity>
    ) {
        viewModelScope.launch {
            repository.saveQuote(
                quote = OrderQuoteEntity(
                    customerName = customerName.trim().ifBlank { "Cliente" },
                    eventDateText = eventDateText.trim(),
                    deliveryFee = deliveryFee.coerceAtLeast(0.0),
                    discountAmount = discountAmount.coerceAtLeast(0.0),
                    notes = notes.trim()
                ),
                items = items
            )
            _feedbackMessage.value = "Orçamento salvo!"
        }
    }

    fun deleteQuote(quoteId: Long) {
        viewModelScope.launch {
            repository.deleteQuote(quoteId)
            _feedbackMessage.value = "Orçamento removido."
        }
    }

    fun updateSettings(settings: BusinessSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings)
            _feedbackMessage.value = "Configurações salvas!"
        }
    }

    fun loadStarterCatalog() {
        viewModelScope.launch {
            repository.loadStarterConfectioneryCatalog()
            _feedbackMessage.value = "Exemplos de confeitaria adicionados!"
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = PrecificaDoceDatabase.getInstance(application)
                    val repo = PrecificaRepository(
                        ingredientDao = db.ingredientDao(),
                        recipeDao = db.recipeDao(),
                        quoteDao = db.quoteDao()
                    )
                    val settingsRepo = BusinessSettingsRepository(application)
                    return PrecificaViewModel(repo, settingsRepo) as T
                }
            }
    }
}
