package com.example.data.repository

import com.example.data.local.IngredientDao
import com.example.data.local.IngredientEntity
import com.example.data.local.OrderQuoteEntity
import com.example.data.local.OrderQuoteItemEntity
import com.example.data.local.QuoteDao
import com.example.data.local.QuoteWithItems
import com.example.data.local.RecipeDao
import com.example.data.local.RecipeEntity
import com.example.data.local.RecipeIngredientEntity
import com.example.data.local.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

class PrecificaRepository(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao,
    private val quoteDao: QuoteDao
) {
    val ingredients: Flow<List<IngredientEntity>> = ingredientDao.observeAll()
    val recipes: Flow<List<RecipeWithIngredients>> = recipeDao.observeAllWithIngredients()
    val quotes: Flow<List<QuoteWithItems>> = quoteDao.observeAllWithItems()

    fun observeRecipe(recipeId: Long): Flow<RecipeWithIngredients?> =
        recipeDao.observeByIdWithIngredients(recipeId)

    suspend fun saveIngredient(ingredient: IngredientEntity): Long {
        val withTimestamp = ingredient.copy(updatedAt = System.currentTimeMillis())
        return if (ingredient.id == 0L) {
            ingredientDao.insert(withTimestamp)
        } else {
            ingredientDao.update(withTimestamp)
            ingredient.id
        }
    }

    suspend fun countIngredientUsages(ingredientId: Long): Int =
        ingredientDao.countRecipeUsages(ingredientId)

    suspend fun deleteIngredient(ingredientId: Long) {
        ingredientDao.deleteById(ingredientId)
    }

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        items: List<RecipeIngredientEntity>
    ): Long {
        return recipeDao.saveRecipeWithIngredients(
            recipe = recipe.copy(updatedAt = System.currentTimeMillis()),
            items = items
        )
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipeById(recipeId)
    }

    suspend fun saveQuote(
        quote: OrderQuoteEntity,
        items: List<OrderQuoteItemEntity>
    ): Long {
        return quoteDao.saveQuoteWithItems(quote, items)
    }

    suspend fun deleteQuote(quoteId: Long) {
        quoteDao.deleteQuoteById(quoteId)
    }

    /**
     * Imports a starter Brazilian patisserie catalog only when explicitly requested by the user.
     */
    suspend fun loadStarterConfectioneryCatalog() {
        if (ingredientDao.getAllOnce().isNotEmpty()) return

        val leiteCondensadoId = ingredientDao.insert(
            IngredientEntity(
                name = "Leite Condensado Integral",
                category = "Laticínios",
                packageQuantity = 395.0,
                packageUnit = "g",
                packagePrice = 7.49
            )
        )
        val cremeLeiteId = ingredientDao.insert(
            IngredientEntity(
                name = "Creme de Leite UHT",
                category = "Laticínios",
                packageQuantity = 200.0,
                packageUnit = "g",
                packagePrice = 4.29
            )
        )
        val manteigaId = ingredientDao.insert(
            IngredientEntity(
                name = "Manteiga sem Sal Extra",
                category = "Laticínios",
                packageQuantity = 200.0,
                packageUnit = "g",
                packagePrice = 11.90
            )
        )
        val chocolate50Id = ingredientDao.insert(
            IngredientEntity(
                name = "Chocolate em Pó 50% Cacau",
                category = "Chocolates",
                packageQuantity = 500.0,
                packageUnit = "g",
                packagePrice = 24.90
            )
        )
        val chocolateNobreId = ingredientDao.insert(
            IngredientEntity(
                name = "Chocolate Meio Amargo Nobre",
                category = "Chocolates",
                packageQuantity = 1.0,
                packageUnit = "kg",
                packagePrice = 58.00
            )
        )
        val granuladoId = ingredientDao.insert(
            IngredientEntity(
                name = "Granulado Belga Split",
                category = "Chocolates",
                packageQuantity = 500.0,
                packageUnit = "g",
                packagePrice = 34.00
            )
        )
        val ovosId = ingredientDao.insert(
            IngredientEntity(
                name = "Ovos Brancos Grandes",
                category = "Secos & Ovos",
                packageQuantity = 30.0,
                packageUnit = "un",
                packagePrice = 21.00
            )
        )
        val farinhaId = ingredientDao.insert(
            IngredientEntity(
                name = "Farinha de Trigo Tipo 1",
                category = "Secos & Ovos",
                packageQuantity = 1.0,
                packageUnit = "kg",
                packagePrice = 5.90
            )
        )
        val acucarId = ingredientDao.insert(
            IngredientEntity(
                name = "Açúcar Refinado",
                category = "Secos & Ovos",
                packageQuantity = 1.0,
                packageUnit = "kg",
                packagePrice = 4.90
            )
        )

        recipeDao.saveRecipeWithIngredients(
            recipe = RecipeEntity(
                name = "Brigadeiro Gourmet Tradicional",
                category = "Brigadeiros",
                yieldQuantity = 30.0,
                yieldUnit = "unidades",
                prepTimeMinutes = 45,
                packagingCost = 9.50,
                overheadPercent = 15.0,
                profitMarkupPercent = 110.0,
                cardFeePercent = 4.99,
                notes = "Peso padrão de 18g boleado com forminha 4 pétalas."
            ),
            items = listOf(
                RecipeIngredientEntity(0, 0, leiteCondensadoId, 395.0, "g"),
                RecipeIngredientEntity(0, 0, cremeLeiteId, 100.0, "g"),
                RecipeIngredientEntity(0, 0, manteigaId, 20.0, "g"),
                RecipeIngredientEntity(0, 0, chocolate50Id, 45.0, "g"),
                RecipeIngredientEntity(0, 0, granuladoId, 120.0, "g")
            )
        )

        recipeDao.saveRecipeWithIngredients(
            recipe = RecipeEntity(
                name = "Brownie Fudge de Chocolate Nobre",
                category = "Brownies",
                yieldQuantity = 16.0,
                yieldUnit = "pedaços",
                prepTimeMinutes = 55,
                packagingCost = 14.40,
                overheadPercent = 18.0,
                profitMarkupPercent = 100.0,
                cardFeePercent = 4.99,
                notes = "Forma 20x20cm cortada em 16 quadrados embalados individualmente."
            ),
            items = listOf(
                RecipeIngredientEntity(0, 0, chocolateNobreId, 250.0, "g"),
                RecipeIngredientEntity(0, 0, manteigaId, 150.0, "g"),
                RecipeIngredientEntity(0, 0, ovosId, 4.0, "un"),
                RecipeIngredientEntity(0, 0, acucarId, 220.0, "g"),
                RecipeIngredientEntity(0, 0, farinhaId, 110.0, "g"),
                RecipeIngredientEntity(0, 0, chocolate50Id, 30.0, "g")
            )
        )
    }
}
