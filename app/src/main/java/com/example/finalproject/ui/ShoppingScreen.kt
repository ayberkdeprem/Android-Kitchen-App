package com.example.finalproject.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.InventoryViewModel
import com.example.finalproject.data.ShoppingItem

@Composable
fun ShoppingScreen(viewModel: InventoryViewModel = viewModel()) {
    val shoppingList by viewModel.shoppingList.collectAsState()
    val context = LocalContext.current

    // --- YENİ: SATIN ALMA DİYALOĞU DEĞİŞKENLERİ ---
    var showBuyDialog by remember { mutableStateOf(false) }
    var itemToBuy by remember { mutableStateOf<ShoppingItem?>(null) }
    var buyAmount by remember { mutableStateOf("") }

    // --- SATIN ALMA DİYALOĞU ---
    if (showBuyDialog && itemToBuy != null) {
        AlertDialog(
            onDismissRequest = { showBuyDialog = false },
            title = { Text("Stoka Ekle") },
            text = {
                Column {
                    Text("Listede görünen miktar: ${itemToBuy!!.quantityNeeded} ${itemToBuy!!.unit}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Gerçekte ne kadar aldın?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buyAmount,
                        onValueChange = { buyAmount = it },
                        label = { Text("Alınan Miktar (${itemToBuy!!.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalAmount = buyAmount.toDoubleOrNull()
                        if (finalAmount != null && finalAmount > 0) {
                            // Gerçek alınan miktarı stoğa ekle
                            // itemToBuy'un bir kopyasını oluşturup miktarını değiştiriyoruz
                            val actualItem = itemToBuy!!.copy(quantityNeeded = finalAmount)
                            viewModel.buyItem(actualItem)

                            Toast.makeText(context, "${actualItem.name}: $finalAmount ${actualItem.unit} eklendi! 📦", Toast.LENGTH_SHORT).show()
                            showBuyDialog = false
                        } else {
                            Toast.makeText(context, "Lütfen geçerli bir miktar girin", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("ONAYLA") }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = false }) { Text("İptal") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // BAŞLIK ALANI
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Alışveriş Listesi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${shoppingList.size} kalem ürün alınacak",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (shoppingList.isEmpty()) {
            // LİSTE BOŞSA
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Harika! Her şey tam.", color = Color.Gray)
                }
            }
        } else {
            // LİSTE DOLUYSA
            LazyColumn {
                items(shoppingList) { item ->
                    ShoppingItemCard(
                        item = item,
                        onBuyClick = {
                            // Tıklanınca hemen alma, diyaloğu aç
                            itemToBuy = item
                            buyAmount = item.quantityNeeded.toString() // Varsayılan olarak listedeki miktarı yaz
                            showBuyDialog = true
                        },
                        onDeleteClick = {
                            viewModel.deleteShoppingItem(item)
                            Toast.makeText(context, "Listeden silindi", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onBuyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.quantityNeeded} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                // SİL BUTONU
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // SATIN AL BUTONU (Artık diyaloğu tetikliyor)
                FilledIconButton(
                    onClick = onBuyClick,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Satın Al", tint = Color.White)
                }
            }
        }
    }
}