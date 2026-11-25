package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE category = :category ORDER BY name ASC")
    fun observeByCategory(category: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    suspend fun getAllOnce(): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: IngredientEntity): Long

    @Update
    suspend fun update(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM recipe_ingredients WHERE ingredientId = :ingredientId")
    suspend fun countRecipeUsages(ingredientId: Long): Int
}

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun observeAllWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY updatedAt DESC")
    fun observeByCategoryWithIngredients(category: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchWithIngredients(query: String): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    fun observeByIdWithIngredients(recipeId: Long): Flow<RecipeWithIngredients?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: Long)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(items: List<RecipeIngredientEntity>)

    @Transaction
    suspend fun saveRecipeWithIngredients(
        recipe: RecipeEntity,
        items: List<RecipeIngredientEntity>
    ): Long {
        val savedId = if (recipe.id == 0L) {
            insertRecipe(recipe)
        } else {
            updateRecipe(recipe)
            deleteRecipeIngredients(recipe.id)
            recipe.id
        }
        val mapped = items.map { it.copy(id = 0, recipeId = savedId) }
        if (mapped.isNotEmpty()) {
            insertRecipeIngredients(mapped)
        }
        return savedId
    }
}

@Dao
interface QuoteDao {
    @Transaction
    @Query("SELECT * FROM order_quotes ORDER BY createdAt DESC")
    fun observeAllWithItems(): Flow<List<QuoteWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: OrderQuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<OrderQuoteItemEntity>)

    @Query("DELETE FROM order_quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: Long)

    @Transaction
    suspend fun saveQuoteWithItems(
        quote: OrderQuoteEntity,
        items: List<OrderQuoteItemEntity>
    ): Long {
        val quoteId = insertQuote(quote)
        val mapped = items.map { it.copy(id = 0, quoteId = quoteId) }
        if (mapped.isNotEmpty()) {
            insertItems(mapped)
        }
        return quoteId
    }
}
