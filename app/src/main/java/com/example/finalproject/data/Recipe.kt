package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val recipeName: String,
    val instructions: String,
    val imageUrl: String? = null,
    val category: String = "Genel" // <-- YENİ: Yemek Kategorisi (Örn: Çorba, Tatlı)
)