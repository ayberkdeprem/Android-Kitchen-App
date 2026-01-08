package com.example.finalproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh // Tarih ikonu yerine temsili
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.InventoryViewModel
import com.example.finalproject.data.UsageLog
import com.example.finalproject.data.UsageStat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalysisScreen(viewModel: InventoryViewModel = viewModel()) {
    val topRecipes by viewModel.topRecipes.collectAsState(initial = emptyList())
    val topIngredients by viewModel.topIngredients.collectAsState(initial = emptyList())
    val recentLogs by viewModel.recentLogs.collectAsState(initial = emptyList()) // <-- YENİ LİSTE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "📊 Tüketim Analizi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Özet veriler ve işlem geçmişi.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        // --- EN ÇOK PİŞEN YEMEKLER (Özet Grafik) ---
        item {
            StatCard(title = "🏆 En Çok Pişen Yemekler", data = topRecipes, isRecipe = true)
        }

        // --- EN ÇOK KULLANILAN MALZEMELER (Özet Grafik) ---
        item {
            StatCard(title = "📉 En Çok Tükenen Malzemeler", data = topIngredients, isRecipe = false)
        }

        // --- İŞLEM GEÇMİŞİ (DETAY LİSTE) ---
        item {
            Text(
                "🕒 Son İşlemler (Detaylı)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (recentLogs.isEmpty()) {
            item { Text("Henüz işlem kaydı yok.", color = Color.Gray) }
        } else {
            items(recentLogs) { log ->
                HistoryItemCard(log)
            }
        }

        // Listenin en altına biraz boşluk bırakalım
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun StatCard(title: String, data: List<UsageStat>, isRecipe: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Text("Veri yok.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                val maxVal = data.maxOf { it.totalAmount }
                data.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                // Grafikte sadece sayı gösteriyoruz, detaylar aşağıda
                                Text(
                                    if (isRecipe) "${item.totalAmount.toInt()} Kez" else String.format("%.1f Br", item.totalAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                            LinearProgressIndicator(
                                progress = (item.totalAmount / maxVal).toFloat(),
                                modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray, RoundedCornerShape(4.dp)),
                                color = if (isRecipe) Color(0xFFFF7043) else Color(0xFF42A5F5),
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- YENİ: GEÇMİŞ İŞLEM KARTI (TARİH VE BİRİM İÇERİR) ---
@Composable
fun HistoryItemCard(log: UsageLog) {
    // Tarihi formatla (Örn: 08 Oca 14:30)
    val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale("tr", "TR")) // Türkçe tarih
    val dateString = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = log.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (log.itemType == "RECIPE") "Yemek Pişti" else "Malzeme Kullanıldı",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // BURADA ARTIK BİRİM VAR! (Örn: 2.0 Adet)
                Text(
                    text = "${log.amount} ${log.unit}",
                    fontWeight = FontWeight.Bold,
                    color = if (log.itemType == "RECIPE") Color(0xFFFF7043) else Color(0xFF42A5F5)
                )
                Text(text = dateString, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }
        }
    }
}