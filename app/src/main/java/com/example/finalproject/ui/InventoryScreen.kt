package com.example.finalproject.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finalproject.InventoryViewModel
import com.example.finalproject.data.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = viewModel()) {
    val inventoryList by viewModel.ingredientList.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = isSystemInDarkTheme() // Koyu mod kontrolü

    val unitOptions = listOf("kg", "gr", "L", "ml", "adet", "paket", "bardak", "kaşık")
    val categoryOptions = listOf("Genel", "Sebze", "Meyve", "Et & Balık", "Süt Ürünü", "Bakliyat", "Baharat", "Atıştırmalık")

    var selectedCategoryFilter by remember { mutableStateOf("Tümü") }
    var searchText by remember { mutableStateOf("") }

    // --- RENK BELİRLEME FONKSİYONU ---
    fun getCategoryColor(category: String): Color {
        if (isDark) return Color(0xFF303030) // Koyu modda çok renkli yapıp göz yormayalım, standart gri kalsın.
        return when (category) {
            "Sebze" -> Color(0xFFE8F5E9)      // Açık Yeşil
            "Meyve" -> Color(0xFFF3E5F5)      // Açık Mor
            "Et & Balık" -> Color(0xFFFFEBEE) // Açık Kırmızı
            "Süt Ürünü" -> Color(0xFFFFFDE7)  // Açık Sarı
            "Bakliyat" -> Color(0xFFEFEBE9)   // Açık Kahve
            "Baharat" -> Color(0xFFFFE0B2)    // Açık Turuncu
            "Atıştırmalık" -> Color(0xFFE0F7FA) // Açık Mavi
            else -> Color(0xFFF5F5F5)         // Gri (Genel)
        }
    }

    fun isLowStock(item: Ingredient): Boolean {
        return when (item.unit) {
            "kg", "L" -> item.quantityDetails < 0.5
            "adet", "paket", "bardak" -> item.quantityDetails < 2.0
            "gr", "ml" -> item.quantityDetails < 200.0
            else -> false
        }
    }

    val displayList = inventoryList.filter { item ->
        val matchesCategory = when (selectedCategoryFilter) {
            "Tümü" -> true
            "⚠️ Kritik" -> isLowStock(item)
            else -> item.category == selectedCategoryFilter
        }
        val matchesSearch = if (searchText.isEmpty()) true else item.name.contains(searchText, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(unitOptions[0]) }
    var category by remember { mutableStateOf(categoryOptions[0]) }
    var isUnitExpanded by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Ingredient?>(null) }
    var editName by remember { mutableStateOf("") }
    var editQuantity by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("") }
    var isEditUnitExpanded by remember { mutableStateOf(false) }
    var isEditCategoryExpanded by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingItem by remember { mutableStateOf<Ingredient?>(null) }

    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Ürünü Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Ürün Adı") })
                    OutlinedTextField(value = editQuantity, onValueChange = { editQuantity = it }, label = { Text("Miktar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    ExposedDropdownMenuBox(expanded = isEditUnitExpanded, onExpandedChange = { isEditUnitExpanded = !isEditUnitExpanded }) {
                        OutlinedTextField(value = editUnit, onValueChange = {}, readOnly = true, label = { Text("Birim") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEditUnitExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = isEditUnitExpanded, onDismissRequest = { isEditUnitExpanded = false }) { unitOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { editUnit = option; isEditUnitExpanded = false }) } }
                    }
                    ExposedDropdownMenuBox(expanded = isEditCategoryExpanded, onExpandedChange = { isEditCategoryExpanded = !isEditCategoryExpanded }) {
                        OutlinedTextField(value = editCategory, onValueChange = {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEditCategoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                        ExposedDropdownMenu(expanded = isEditCategoryExpanded, onDismissRequest = { isEditCategoryExpanded = false }) { categoryOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { editCategory = option; isEditCategoryExpanded = false }) } }
                    }
                }
            },
            confirmButton = { Button(onClick = { if (editName.isNotEmpty()) { val updatedItem = editingItem!!.copy(name = editName, quantityDetails = editQuantity.toDoubleOrNull() ?: 0.0, unit = editUnit, category = editCategory); viewModel.updateIngredient(updatedItem); showEditDialog = false } }) { Text("KAYDET") } },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("İptal") } }
        )
    }

    if (showDeleteDialog && deletingItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Silme İşlemi") },
            text = { Text("${deletingItem!!.name} ürününü silmek istediğine emin misin?") },
            confirmButton = { Button(onClick = { viewModel.deleteIngredient(deletingItem!!); showDeleteDialog = false; deletingItem = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("EVET, SİL") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; deletingItem = null }) { Text("Vazgeç") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Column {
                Text("📦 Mutfak Stoğu", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = searchText, onValueChange = { searchText = it }, label = { Text("Stokta ara...") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf("Tümü", "⚠️ Kritik") + categoryOptions
                    filterOptions.forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        val isCriticalButton = cat == "⚠️ Kritik"
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(text = cat, fontWeight = if (isCriticalButton) FontWeight.Bold else FontWeight.Normal) },
                            enabled = true,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isCriticalButton) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                labelColor = if (isCriticalButton) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = if (isCriticalButton) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isCriticalButton) MaterialTheme.colorScheme.error else Color.Gray)
                        )
                    }
                }

                Card(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ürün Adı") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Miktar") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(expanded = isUnitExpanded, onExpandedChange = { isUnitExpanded = !isUnitExpanded }) {
                                    OutlinedTextField(value = unit, onValueChange = {}, readOnly = true, label = { Text("Birim") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitExpanded) }, modifier = Modifier.menuAnchor(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                                    ExposedDropdownMenu(expanded = isUnitExpanded, onDismissRequest = { isUnitExpanded = false }) { unitOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { unit = option; isUnitExpanded = false }) } }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(expanded = isCategoryExpanded, onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }) {
                            OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                            ExposedDropdownMenu(expanded = isCategoryExpanded, onDismissRequest = { isCategoryExpanded = false }) { categoryOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; isCategoryExpanded = false }) } }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { if (name.isNotEmpty()) { viewModel.addIngredient(name, quantity, unit, category); name = ""; quantity = ""; keyboardController?.hide() } }, modifier = Modifier.fillMaxWidth()) { Text("Stoka Ekle") }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Dolaptakiler (${selectedCategoryFilter}):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (displayList.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Dolap tam takır!\n(Veya aradığın ürün bulunamadı)", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(displayList) { item ->
                val isCritical = isLowStock(item)

                // --- RENK MANTIĞI BURADA ---
                // Eğer kritikse HATA rengi, değilse KATEGORİ rengi
                val cardColor = if (isCritical) MaterialTheme.colorScheme.errorContainer else getCategoryColor(item.category)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                        editingItem = item; editName = item.name; editQuantity = item.quantityDetails.toString(); editUnit = item.unit; editCategory = item.category; showEditDialog = true
                    },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (isCritical) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Warning, contentDescription = "Kritik", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Text(" KRİTİK!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${item.quantityDetails} ${item.unit}", style = MaterialTheme.typography.bodyMedium, color = if(isCritical) MaterialTheme.colorScheme.onErrorContainer else Color.DarkGray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.5f)) {
                                    Text(text = item.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.Black)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.addDirectlyToShoppingList(item); Toast.makeText(context, "${item.name} listeye eklendi (+1)", Toast.LENGTH_SHORT).show() }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Markete Ekle", tint = if(isCritical) MaterialTheme.colorScheme.error else Color(0xFF4CAF50))
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { deletingItem = item; showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }
}