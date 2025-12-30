package com.example.finalproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.Ingredient
import com.example.finalproject.data.Recipe
import com.example.finalproject.data.RecipeRequirement
import com.example.finalproject.data.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    val dao = AppDatabase.getDatabase(application).inventoryDao()

    private val _ingredientList = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredientList: StateFlow<List<Ingredient>> = _ingredientList.asStateFlow()

    private val _recipeList = MutableStateFlow<List<Recipe>>(emptyList())
    val recipeList: StateFlow<List<Recipe>> = _recipeList.asStateFlow()

    private val _shoppingList = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingList: StateFlow<List<ShoppingItem>> = _shoppingList.asStateFlow()

    private val _cookableRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val cookableRecipes: StateFlow<List<Recipe>> = _cookableRecipes.asStateFlow()

    init {
        viewModelScope.launch { dao.getAllIngredients().collect { _ingredientList.value = it } }
        viewModelScope.launch { dao.getAllRecipes().collect { _recipeList.value = it } }
        viewModelScope.launch { dao.getAllShoppingItems().collect { _shoppingList.value = it } }
    }

    // --- ⚖️ BİRİM AĞIRLIK HESAPLAYICI (ÇARPAN) ---
    // Bu fonksiyon sadece 1 birimin kaç gram/ml olduğunu söyler.
    private fun getUnitMultiplier(unit: String, ingredientName: String): Double {
        val normalizedUnit = unit.lowercase().trim()
        val name = ingredientName.lowercase()

        // 1. TEMEL BİRİMLER (Referans: Gram/Ml)
        if (normalizedUnit == "kg" || normalizedUnit == "l" || normalizedUnit == "litre") return 1000.0
        if (normalizedUnit == "gr" || normalizedUnit == "ml" || normalizedUnit == "gram") return 1.0

        // 2. ADET / TANE AĞIRLIKLARI (Ortalama Gramajlar)
        if (normalizedUnit.contains("adet") || normalizedUnit.contains("tane")) {
            return when {
                name.contains("biber") -> 50.0      // 1 sivri biber
                name.contains("kapya") -> 150.0     // 1 kapya
                name.contains("dolmalık") -> 100.0
                name.contains("soğan") -> 150.0
                name.contains("patates") -> 200.0
                name.contains("domates") -> 120.0
                name.contains("havuç") -> 100.0
                name.contains("sarımsak") -> 30.0   // 1 baş
                name.contains("kabak") -> 200.0
                name.contains("patlıcan") -> 250.0
                name.contains("salatalık") -> 120.0
                name.contains("marul") -> 500.0
                name.contains("elma") -> 180.0
                name.contains("limon") -> 80.0
                name.contains("portakal") -> 200.0
                name.contains("muz") -> 150.0
                name.contains("yumurta") -> 50.0    // HATA ÇÖZÜCÜ: 1 Yumurta = 50 gr
                name.contains("ekmek") -> 250.0
                else -> 1.0 // Bilinmiyorsa 1 kabul et
            }
        }

        // 3. MUTFAK ÖLÇÜLERİ
        if (normalizedUnit.contains("bardak") || normalizedUnit.contains("su bardağı")) {
            return when {
                name.contains("un") -> 110.0
                name.contains("şeker") -> 200.0
                name.contains("yağ") -> 200.0
                name.contains("süt") -> 200.0
                name.contains("su") -> 200.0
                name.contains("pirinç") -> 180.0
                name.contains("mercimek") -> 170.0
                name.contains("bulgur") -> 160.0
                else -> 200.0
            }
        }

        // KAŞIKLAR (YK, TK, ÇK)
        if (normalizedUnit.contains("yemek kaşığı") || normalizedUnit == "yk" || (normalizedUnit.contains("kaşık") && !normalizedUnit.contains("tatlı") && !normalizedUnit.contains("çay"))) {
            return when {
                name.contains("salça") -> 25.0
                name.contains("tuz") -> 18.0
                name.contains("şeker") -> 15.0
                name.contains("un") -> 10.0
                name.contains("yağ") -> 12.0
                name.contains("kakao") -> 8.0
                else -> 15.0
            }
        }
        if (normalizedUnit.contains("tatlı kaşığı") || normalizedUnit == "tk") {
            return when {
                name.contains("salça") -> 15.0
                name.contains("tuz") -> 12.0
                name.contains("şeker") -> 10.0
                name.contains("un") -> 7.0
                name.contains("yağ") -> 8.0
                name.contains("kakao") -> 5.0
                else -> 10.0
            }
        }
        if (normalizedUnit.contains("çay kaşığı") || normalizedUnit == "çk") {
            return when {
                name.contains("tuz") -> 6.0
                name.contains("şeker") -> 5.0
                name.contains("kabartma") -> 5.0
                else -> 5.0
            }
        }

        if (normalizedUnit.contains("paket")) {
            return when {
                name.contains("vanilya") -> 5.0
                name.contains("kabartma") -> 10.0
                name.contains("makarna") -> 500.0
                name.contains("krema") -> 200.0
                name.contains("margarin") -> 250.0
                else -> 1.0
            }
        }

        if (normalizedUnit.contains("demet") || normalizedUnit.contains("bağ")) return 50.0

        return 1.0 // Varsayılan çarpan
    }

    // --- PİŞİRME FONKSİYONU (DÜZELTİLDİ: GERİ DÖNÜŞÜM EKLENDİ) ---
    fun cookRecipe(recipe: Recipe, portions: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val requirements = dao.getRequirementsForRecipe(recipe.recipeId)
                val currentInventory = dao.getAllIngredientsForCheck()
                val missingItems = mutableListOf<String>()

                // 1. ÖNCE KONTROL ET (Yeterli mi?)
                for (req in requirements) {
                    var stockItem = currentInventory.find { it.ingredientId == req.ingredientId }
                    if (stockItem == null) stockItem = currentInventory.find { it.name.equals(req.ingredientName, ignoreCase = true) }

                    val totalRequired = req.requiredAmount * portions

                    if (stockItem == null) {
                        missingItems.add("Eksik: ${req.ingredientName}")
                    } else {
                        // Her ikisini de GRAM'a çevirip karşılaştır
                        val stockMultiplier = getUnitMultiplier(stockItem.unit, stockItem.name)
                        val reqMultiplier = getUnitMultiplier(req.unit, req.ingredientName)

                        val stockInGram = stockItem.quantityDetails * stockMultiplier
                        val reqInGram = totalRequired * reqMultiplier

                        if (stockInGram < reqInGram - 0.1) {
                            missingItems.add("Yetersiz: ${req.ingredientName}")
                        }
                    }
                }

                if (missingItems.isNotEmpty()) {
                    withContext(Dispatchers.Main) { onError(missingItems.joinToString("\n")) }
                } else {
                    // 2. YETERLİYSE STOKTAN DÜŞ (GÜNCELLENEN KISIM)
                    for (req in requirements) {
                        var stockItem = currentInventory.find { it.ingredientId == req.ingredientId }
                        if (stockItem == null) stockItem = currentInventory.find { it.name.equals(req.ingredientName, ignoreCase = true) }

                        if (stockItem != null) {
                            val totalRequired = req.requiredAmount * portions

                            // Çarpanları al
                            val stockMultiplier = getUnitMultiplier(stockItem.unit, stockItem.name)
                            val reqMultiplier = getUnitMultiplier(req.unit, req.ingredientName)

                            // İşlemi GRAM üzerinden yap
                            val stockInGram = stockItem.quantityDetails * stockMultiplier
                            val reqInGram = totalRequired * reqMultiplier

                            var remainingInGram = stockInGram - reqInGram
                            if (remainingInGram < 0) remainingInGram = 0.0

                            // --- KRİTİK DÜZELTME BURASI ---
                            // Kalan GRAM'ı, stoktaki birime geri çevir!
                            // Örn: 650 gram kaldı / 50 (yumurta ağırlığı) = 13 Adet
                            val newQuantity = remainingInGram / stockMultiplier

                            dao.updateIngredient(stockItem.copy(quantityDetails = newQuantity))
                        }
                    }
                    withContext(Dispatchers.Main) { onSuccess() }
                }
            }
        }
    }

    // --- EKSİK LİSTESİ ---
    fun addMissingToShoppingList(recipe: Recipe, portions: Int, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val requirements = dao.getRequirementsForRecipe(recipe.recipeId)
            val currentInventory = dao.getAllIngredientsForCheck()
            var addedCount = 0

            for (req in requirements) {
                var stockItem = currentInventory.find { it.ingredientId == req.ingredientId }
                if (stockItem == null) stockItem = currentInventory.find { it.name.equals(req.ingredientName, ignoreCase = true) }

                val totalRequired = req.requiredAmount * portions

                if (stockItem == null) {
                    dao.insertShoppingItem(ShoppingItem(name = req.ingredientName, quantityNeeded = totalRequired, unit = req.unit))
                    addedCount++
                } else {
                    val stockMultiplier = getUnitMultiplier(stockItem.unit, stockItem.name)
                    val reqMultiplier = getUnitMultiplier(req.unit, req.ingredientName)

                    val stockInGram = stockItem.quantityDetails * stockMultiplier
                    val reqInGram = totalRequired * reqMultiplier

                    if (stockInGram < reqInGram) {
                        val missingInGram = reqInGram - stockInGram

                        // Eksik miktarı TARİFİN BİRİMİNE geri çevirerek listeye ekle
                        val missingAmount = missingInGram / reqMultiplier

                        dao.insertShoppingItem(ShoppingItem(name = stockItem.name, quantityNeeded = missingAmount, unit = req.unit))
                        addedCount++
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (addedCount > 0) onResult("📝 $addedCount eksik market listesine eklendi!")
                else onResult("✅ Bu porsiyon için elindeki malzemeler yeterli.")
            }
        }
    }

    // --- YAPILABİLİR YEMEKLER ---
    fun calculateCookableRecipes() {
        viewModelScope.launch(Dispatchers.IO) {
            val allRecipes = dao.getAllRecipesSync()
            val allIngredients = dao.getAllIngredientsForCheck()

            val cookable = allRecipes.filter { recipe ->
                val requirements = dao.getRequirementsForRecipe(recipe.recipeId)
                requirements.all { req ->
                    val stockItem = allIngredients.find { it.ingredientId == req.ingredientId }
                        ?: allIngredients.find { it.name.equals(req.ingredientName, ignoreCase = true) }

                    if (stockItem == null) false
                    else {
                        val stockMultiplier = getUnitMultiplier(stockItem.unit, stockItem.name)
                        val reqMultiplier = getUnitMultiplier(req.unit, req.ingredientName)

                        val stockInGram = stockItem.quantityDetails * stockMultiplier
                        val reqInGram = req.requiredAmount * reqMultiplier

                        stockInGram >= reqInGram
                    }
                }
            }
            _cookableRecipes.value = cookable
        }
    }

    // --- YARDIMCI FONKSİYONLAR ---
    fun addIngredient(name: String, quantity: String, unit: String, category: String) {
        val qty = quantity.toDoubleOrNull() ?: 0.0
        viewModelScope.launch { dao.insertIngredient(Ingredient(name = name, quantityDetails = qty, unit = unit, category = category)) }
    }
    fun updateIngredient(item: Ingredient) { viewModelScope.launch { dao.updateIngredient(item) } }
    fun deleteIngredient(item: Ingredient) { viewModelScope.launch { dao.deleteIngredient(item) } }

    fun addRecipe(name: String, instructions: String, imageUrl: String, category: String) {
        viewModelScope.launch { dao.insertRecipe(Recipe(recipeName = name, instructions = instructions, imageUrl = imageUrl, category = category)) }
    }
    fun deleteRecipe(recipe: Recipe) { viewModelScope.launch { dao.deleteRecipe(recipe) } }

    fun addRequirementToRecipe(recipeId: Int, ingredientId: Int, name: String, amount: Double, unit: String) {
        viewModelScope.launch { dao.insertRecipeRequirement(RecipeRequirement(recipeId = recipeId, ingredientId = ingredientId, ingredientName = name, requiredAmount = amount, unit = unit)) }
    }
    fun deleteRecipeRequirement(req: RecipeRequirement) { viewModelScope.launch { dao.deleteRecipeRequirement(req) } }
    fun updateRecipeRequirement(req: RecipeRequirement) { viewModelScope.launch { dao.updateRecipeRequirement(req) } }

    fun getRecipeIngredientsFlow(recipeId: Int): Flow<List<RecipeRequirement>> {
        return dao.getRecipeIngredients(recipeId)
    }

    fun buyItem(item: ShoppingItem) {
        viewModelScope.launch {
            val existing = dao.getIngredientByName(item.name)
            if (existing != null) {
                // Alınan ürünün birimi ile stoktaki birim farklı olabilir.
                // Basitlik için: Eğer isim aynıysa direkt üstüne ekliyoruz.
                // (Mühendislik Notu: Burada da birim çevirimi yapılabilir ama genelde marketten aldığın birimle stoğun aynı olur)
                val newQty = existing.quantityDetails + item.quantityNeeded
                dao.updateIngredient(existing.copy(quantityDetails = newQty))
            } else {
                dao.insertIngredient(Ingredient(name = item.name, quantityDetails = item.quantityNeeded, unit = item.unit, category = "Genel"))
            }
            dao.deleteShoppingItem(item)
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) { viewModelScope.launch { dao.deleteShoppingItem(item) } }

    fun addDirectlyToShoppingList(item: Ingredient) {
        viewModelScope.launch {
            dao.insertShoppingItem(ShoppingItem(name = item.name, quantityNeeded = 1.0, unit = item.unit))
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            dao.updateRecipe(recipe)
            // loadRecipes()  <-- BU SATIRI SİLDİK, ARTIK HATA VERMEYECEK

            calculateCookableRecipes() // Bunu tutuyoruz, "yapılabilir yemekleri" tekrar hesaplasın.
        }
    }
}