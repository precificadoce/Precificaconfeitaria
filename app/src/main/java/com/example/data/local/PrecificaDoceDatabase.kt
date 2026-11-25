package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        OrderQuoteEntity::class,
        OrderQuoteItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PrecificaDoceDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: PrecificaDoceDatabase? = null

        fun getInstance(context: Context): PrecificaDoceDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PrecificaDoceDatabase::class.java,
                    "precificadoce.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
