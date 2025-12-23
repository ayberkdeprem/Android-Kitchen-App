package com.example.finalproject.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.finalproject.InventoryViewModel
import com.example.finalproject.data.Recipe
import com.example.finalproject.data.RecipeRequirement
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: InventoryViewModel = viewModel(),
    onNavigateToInventory: () -> Unit,
    // --- YENİ EKLENEN PARAMETRELER ---
    isDarkTheme: Boolean,
    onThemeChanged: () -> Unit
) {
    val allRecipes by viewModel.recipeList.collectAsState()
    val cookableRecipes by viewModel.cookableRecipes.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }

    val recipeCategories = listOf("Tümü", "Ana Yemek", "Çorba", "Ara Sıcak", "Salata", "Tatlı", "İçecek", "Kahvaltı")
    var selectedCategoryFilter by remember { mutableStateOf("Tümü") }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.calculateCookableRecipes()
    }

    val baseList = if (selectedTab == 0) allRecipes else cookableRecipes

    val displayList = baseList.filter { recipe ->
        val matchesSearch = if (searchText.isEmpty()) true else recipe.recipeName.contains(searchText, ignoreCase = true)
        val matchesCategory = if (selectedCategoryFilter == "Tümü") true else recipe.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- BAŞLIK KISMI GÜNCELLENDİ (GÜNEŞ/AY BUTONU EKLENDİ) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Merhaba Şef! 👨‍🍳",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // TEMA DEĞİŞTİRME BUTONU
            IconButton(onClick = onThemeChanged) {
                Icon(
                    // Eğer tema Koyu ise -> Güneş göster (Aydınlığa geçmek için)
                    // Eğer tema Açık ise -> Ay göster (Karanlığa geçmek için)
                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Tema Değiştir",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText, onValueChange = { searchText = it },
            label = { Text("Yemek ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // KATEGORİ BUTONLARI
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recipeCategories.forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = MaterialTheme.colorScheme.primary) }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tüm Menü") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("✨ Yapabileceklerim") })
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 1 && displayList.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text("😔 Aradığın kriterde yapılabilir yemek bulunamadı.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        LazyColumn {
            items(displayList) { recipe ->
                ExpandableRecipeCard(recipe = recipe, viewModel = viewModel, context = context, isSuggestion = selectedTab == 1)
            }
        }
    }
}

// ExpandableRecipeCard kodları aşağıda aynı şekilde kalacak,
// Sadece dosya bütünlüğünü bozmamak için buraya tekrar yapıştırmıyorum.
// Önceki HomeScreen kodundaki ExpandableRecipeCard fonksiyonunu buranın altına eklemelisin
// (Eğer tek dosyadaysa. Ayrı dosyadaysa dokunmana gerek yok).
// NOT: Eğer önceki kodun devamı sendeyse onu kullanabilirsin, yoksa aşağıya ekleyebilirim.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableRecipeCard(
    recipe: Recipe,
    viewModel: InventoryViewModel,
    context: android.content.Context,
    isSuggestion: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var portionCount by remember { mutableStateOf(1) }

    val ingredients by viewModel.getRecipeIngredientsFlow(recipe.recipeId).collectAsState(initial = emptyList())
    val inventory by viewModel.ingredientList.collectAsState()

    val isSufficient = remember(portionCount, ingredients, inventory) {
        if (ingredients.isEmpty()) true else {
            ingredients.all { req ->
                val stockItem = inventory.find { it.ingredientId == req.ingredientId }
                    ?: inventory.find { it.name.equals(req.ingredientName, ignoreCase = true) }

                if (stockItem == null) {
                    false
                } else {
                    val stockBase = if (stockItem.unit.equals("kg", true) || stockItem.unit.equals("L", true)) stockItem.quantityDetails * 1000 else stockItem.quantityDetails
                    val reqBasePerPortion = if (req.unit.equals("kg", true) || req.unit.equals("L", true)) req.requiredAmount * 1000 else req.requiredAmount

                    val totalNeeded = reqBasePerPortion * portionCount
                    stockBase >= totalNeeded
                }
            }
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<RecipeRequirement?>(null) }
    var editAmount by remember { mutableStateOf("") }
    var editUnit by remember { mutableStateOf("") }
    var isUnitExpanded by remember { mutableStateOf(false) }
    val unitOptions = listOf("kg", "gr", "L", "ml", "adet", "paket", "bardak", "yk", "tk", "çk")

    if (showEditDialog && editingItem != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Malzemeyi Düzenle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editAmount, onValueChange = { editAmount = it }, label = { Text("Miktar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    ExposedDropdownMenuBox(expanded = isUnitExpanded, onExpandedChange = { isUnitExpanded = !isUnitExpanded }) {
                        OutlinedTextField(value = editUnit, onValueChange = {}, readOnly = true, label = { Text("Birim") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitExpanded) }, modifier = Modifier.menuAnchor())
                        ExposedDropdownMenu(expanded = isUnitExpanded, onDismissRequest = { isUnitExpanded = false }) { unitOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { editUnit = option; isUnitExpanded = false }) } }
                    }
                }
            },
            confirmButton = { Button(onClick = { viewModel.updateRecipeRequirement(editingItem!!.copy(requiredAmount = editAmount.toDoubleOrNull() ?: 0.0, unit = editUnit)); showEditDialog = false }) { Text("KAYDET") } },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("İptal") } }
        )
    }

    val cardColor = if (isSuggestion) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
    // Koyu modda suggestion kartının rengi çok parlak kalabilir, onu dinamik yapmak daha iyi olur ama şimdilik böyle kalsın.

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSuggestion && !isSystemInDarkTheme()) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface) // Ufak bir koyu mod düzeltmesi
    ) {
        Column {
            if (!recipe.imageUrl.isNullOrEmpty()) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(recipe.imageUrl).crossfade(true).error(android.R.drawable.ic_menu_report_image).build(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Crop)
            } else { Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.primary)) }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = recipe.recipeName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(text = recipe.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    if (isSuggestion) Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF4CAF50))
                }

                if (expanded) {
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("📋 Malzemeler (1 Kişilik):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    ingredients.forEach { req ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                            val totalNeeded = req.requiredAmount * portionCount
                            val displayText = if (portionCount > 1) {
                                "${req.ingredientName}: ${req.requiredAmount} ${req.unit} (x$portionCount = $totalNeeded)"
                            } else {
                                "${req.ingredientName}: ${req.requiredAmount} ${req.unit}"
                            }

                            Text(text = "- $displayText", style = MaterialTheme.typography.bodyMedium)

                            Row {
                                IconButton(onClick = { editingItem = req; editAmount = req.requiredAmount.toString(); editUnit = req.unit; showEditDialog = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                                IconButton(onClick = { viewModel.deleteRecipeRequirement(req) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red, modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledIconButton(
                            onClick = { if (portionCount > 1) portionCount-- },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("-", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "$portionCount Kişilik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        FilledIconButton(
                            onClick = { portionCount++ },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Artır")
                        }
                    }

                    if (!isSufficient) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "⚠️ Yetersiz Stok! Bu miktar için malzemen eksik.",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.cookRecipe(
                                    recipe,
                                    portionCount,
                                    onSuccess = { Toast.makeText(context, "✅ $portionCount Kişilik Üretim Tamamlandı!", Toast.LENGTH_LONG).show(); if (isSuggestion) viewModel.calculateCookableRecipes() },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = isSufficient,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text(if (isSufficient) "PİŞİR" else "STOK YOK")
                        }

                        OutlinedButton(
                            onClick = { viewModel.addMissingToShoppingList(recipe, portionCount) { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() } },
                            modifier = Modifier.weight(1f)
                        ) { Text("EKSİKLER") }
                    }
                }
            }
        }
    }
}