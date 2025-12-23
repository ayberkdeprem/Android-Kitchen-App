package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val ingredientId: Int = 0,
    val name: String,
    val quantityDetails: Double,
    val unit: String,
    val category: String = "Genel" // <-- YENİ: Kategori Alanı (Varsayılan: Genel)
)