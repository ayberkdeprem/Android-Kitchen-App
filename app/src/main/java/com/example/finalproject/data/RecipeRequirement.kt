package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_requirements")
data class RecipeRequirement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val ingredientId: Int,
    val ingredientName: String,
    val requiredAmount: Double,
    val unit: String = "" // <-- YENİ EKLENEN: İstenen birim (gr, kg, adet)
)