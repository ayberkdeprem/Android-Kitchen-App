package com.example.finalproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val itemName: String,      // Yemek veya Malzeme adı
    val itemType: String,      // "RECIPE" (Yemek) veya "INGREDIENT" (Malzeme)
    val amount: Double,        // Ne kadar kullanıldı? (Örn: 2 porsiyon veya 500 gr)
    val unit: String,
    val timestamp: Long = System.currentTimeMillis() // Ne zaman? (Tarih analizi için)
)

// İstatistikleri çekmek için yardımcı bir sınıf (Veritabanından dönen özet veri)
data class UsageStat(
    val name: String,
    val totalAmount: Double
)