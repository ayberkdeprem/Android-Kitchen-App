package com.example.finalproject.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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
import com.example.finalproject.data.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: InventoryViewModel = viewModel()) {
    val recipeList by viewModel.recipeList.collectAsState()
    val inventoryList by viewModel.ingredientList.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val unitOptions = listOf("kg", "gr", "L", "ml", "adet", "paket", "bardak", "yk", "tk", "çk")
    val recipeCategories = listOf("Ana Yemek", "Çorba", "Ara Sıcak", "Salata", "Tatlı", "İçecek", "Kahvaltı")

    var newRecipeName by remember { mutableStateOf("") }
    var newRecipeUrl by remember { mutableStateOf("") }
    var newRecipeCategory by remember { mutableStateOf(recipeCategories[0]) }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    if (showDeleteDialog && recipeToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Tarifi Sil") },
            text = { Text("${recipeToDelete!!.recipeName} tarifini silmek istediğine emin misin? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecipe(recipeToDelete!!)
                        Toast.makeText(context, "Tarif Silindi 🗑️", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("EVET, SİL") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false; recipeToDelete = null }) { Text("Vazgeç") } }
        )
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = "recipe_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val internalPath = saveImageToInternalStorage(context, uri)
            if (internalPath != null) {
                newRecipeUrl = internalPath
                Toast.makeText(context, "Resim Yüklendi! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var requiredAmount by remember { mutableStateOf("") }
    var requiredUnit by remember { mutableStateOf(unitOptions[0]) }

    var isRecipeExpanded by remember { mutableStateOf(false) }
    var isIngredientExpanded by remember { mutableStateOf(false) }
    var isUnitExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Column {
                Text("👨‍🍳 Şefin Menüsü", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Card(modifier = Modifier.padding(top = 16.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yeni Yemek Tanımla", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(value = newRecipeName, onValueChange = { newRecipeName = it }, label = { Text("Yemek Adı") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(expanded = isCategoryExpanded, onExpandedChange = { isCategoryExpanded = !isCategoryExpanded }) {
                            OutlinedTextField(value = newRecipeCategory, onValueChange = {}, readOnly = true, label = { Text("Kategori") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                            ExposedDropdownMenu(expanded = isCategoryExpanded, onDismissRequest = { isCategoryExpanded = false }) {
                                recipeCategories.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { newRecipeCategory = cat; isCategoryExpanded = false }) }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = newRecipeUrl, onValueChange = { newRecipeUrl = it }, label = { Text("Resim Yolu") }, placeholder = { Text("Link veya Galeri...") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White))
                            Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Icon(Icons.Default.Add, "Galeri"); Text("Seç") }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newRecipeName.isNotEmpty()) {
                                    viewModel.addRecipe(newRecipeName, "", newRecipeUrl, newRecipeCategory)
                                    newRecipeName = ""; newRecipeUrl = ""; keyboardController?.hide()
                                    Toast.makeText(context, "Yemek Kaydedildi", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Yemeği Kaydet") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Reçete Bağla", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().padding(top=8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(expanded = isRecipeExpanded, onExpandedChange = { isRecipeExpanded = !isRecipeExpanded }) {
                            OutlinedTextField(value = selectedRecipe?.recipeName ?: "Yemek Seç", onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRecipeExpanded) }, modifier = Modifier.menuAnchor())
                            ExposedDropdownMenu(expanded = isRecipeExpanded, onDismissRequest = { isRecipeExpanded = false }) {
                                recipeList.forEach { recipe -> DropdownMenuItem(text = { Text(recipe.recipeName) }, onClick = { selectedRecipe = recipe; isRecipeExpanded = false }) }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(expanded = isIngredientExpanded, onExpandedChange = { isIngredientExpanded = !isIngredientExpanded }) {
                            OutlinedTextField(value = selectedIngredient?.name ?: "Malzeme Seç", onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIngredientExpanded) }, modifier = Modifier.menuAnchor())
                            ExposedDropdownMenu(expanded = isIngredientExpanded, onDismissRequest = { isIngredientExpanded = false }) {
                                inventoryList.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { selectedIngredient = item; requiredUnit = item.unit; isIngredientExpanded = false }) }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = requiredAmount, onValueChange = { requiredAmount = it }, label = { Text("Miktar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(expanded = isUnitExpanded, onExpandedChange = { isUnitExpanded = !isUnitExpanded }) {
                            OutlinedTextField(value = requiredUnit, onValueChange = {}, readOnly = true, label = { Text("Birim") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitExpanded) }, modifier = Modifier.menuAnchor())
                            ExposedDropdownMenu(expanded = isUnitExpanded, onDismissRequest = { isUnitExpanded = false }) {
                                unitOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { requiredUnit = option; isUnitExpanded = false }) }
                            }
                        }
                    }
                }

                Button(onClick = {
                    if (selectedRecipe != null && selectedIngredient != null && requiredAmount.isNotEmpty()) {
                        viewModel.addRequirementToRecipe(selectedRecipe!!.recipeId, selectedIngredient!!.ingredientId, selectedIngredient!!.name, requiredAmount.toDoubleOrNull() ?: 0.0, requiredUnit)
                        keyboardController?.hide()
                        Toast.makeText(context, "Eklendi!", Toast.LENGTH_SHORT).show()
                        selectedIngredient = null; requiredAmount = ""
                    }
                }, modifier = Modifier.fillMaxWidth().padding(top=8.dp)) { Text("Reçeteye Ekle (+)") }

                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }

        // --- YENİ EKLENEN KISIM: BOŞ DURUM KONTROLÜ ---
        if (recipeList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz hiç tarif eklemedin.\nYukarıdan ilk şaheserini yarat!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(recipeList) { recipe ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = recipe.recipeName, style = MaterialTheme.typography.titleLarge)
                            Text(text = recipe.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { recipeToDelete = recipe; showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}