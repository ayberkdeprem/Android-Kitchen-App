package com.example.finalproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // --- CANLI VERİ AKIŞLARI (UI İÇİN) ---
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM recipes ORDER BY recipeName ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM shopping_list ORDER BY name ASC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM recipe_requirements WHERE recipeId = :recipeId")
    fun getRecipeIngredients(recipeId: Int): Flow<List<RecipeRequirement>>

    // --- ANLIK VERİ ÇEKME (HESAPLAMA İÇİN - YENİ EKLENENLER) ---

    // Hata veren fonksiyon buydu:
    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesSync(): List<Recipe>

    // ViewModel'deki diğer hesaplamalar için gerekli:
    @Query("SELECT * FROM ingredients")
    suspend fun getAllIngredientsForCheck(): List<Ingredient>

    @Query("SELECT * FROM recipe_requirements WHERE recipeId = :recipeId")
    suspend fun getRequirementsForRecipe(recipeId: Int): List<RecipeRequirement>

    @Query("SELECT * FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): Ingredient?

    // --- EKLEME / GÜNCELLEME / SİLME ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeRequirement(requirement: RecipeRequirement)

    @Update
    suspend fun updateRecipeRequirement(requirement: RecipeRequirement)

    @Delete
    suspend fun deleteRecipeRequirement(requirement: RecipeRequirement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)
}