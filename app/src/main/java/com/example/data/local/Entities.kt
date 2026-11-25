package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val packageQuantity: Double,
    val packageUnit: String,
    val packagePrice: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val yieldQuantity: Double,
    val yieldUnit: String,
    val prepTimeMinutes: Int,
    val packagingCost: Double,
    val overheadPercent: Double,
    val profitMarkupPercent: Double,
    val cardFeePercent: Double,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recipeId"]),
        Index(value = ["ingredientId"])
    ]
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val ingredientId: Long,
    val quantityUsed: Double,
    val unitUsed: String
)

data class RecipeIngredientWithIngredient(
    @Embedded val recipeIngredient: RecipeIngredientEntity,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "id"
    )
    val ingredient: IngredientEntity
)

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        entity = RecipeIngredientEntity::class,
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val items: List<RecipeIngredientWithIngredient>
)

@Entity(tableName = "order_quotes")
data class OrderQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val eventDateText: String,
    val deliveryFee: Double,
    val discountAmount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_quote_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderQuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quoteId"])]
)
data class OrderQuoteItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long,
    val recipeId: Long?,
    val itemName: String,
    val quantity: Int,
    val unitCost: Double,
    val unitPrice: Double
)

data class QuoteWithItems(
    @Embedded val quote: OrderQuoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "quoteId"
    )
    val items: List<OrderQuoteItemEntity>
)
