package com.example.finalproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// YENİ EKLENEN: ShoppingItem::class
@Database(entities = [Ingredient::class, Recipe::class, RecipeRequirement::class, ShoppingItem::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Veritabanı yapısı değiştiği için "fallbackToDestructiveMigration" ekledik
                // Bu, eski verileri silip yenisini kurar (Geliştirme aşaması için ideal)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kitchen_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}