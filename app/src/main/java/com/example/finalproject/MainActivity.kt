package com.example.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.finalproject.ui.HomeScreen
import com.example.finalproject.ui.InventoryScreen
import com.example.finalproject.ui.RecipeScreen
import com.example.finalproject.ui.ShoppingScreen
import com.example.finalproject.ui.theme.FinalProjectTheme
import androidx.compose.material.icons.filled.Add

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // --- TEMA YÖNETİMİ BURADA ---
            // Varsayılan olarak 'false' yani Açık Tema. 'true' yaparsan Koyu başlar.
            var isDarkTheme by remember { mutableStateOf(false) }

            // Tüm uygulamayı sarmalayan Tema
            FinalProjectTheme(darkTheme = isDarkTheme) {

                val navController = rememberNavController()
                val viewModel: InventoryViewModel = viewModel()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Menü") },
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Add, contentDescription = "Tarifler") }, // Add ikonu Tarif Ekleme için
                                label = { Text("Tarif Ekle") },
                                selected = currentRoute == "recipes",
                                onClick = { navController.navigate("recipes") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Envanter") },
                                label = { Text("Stok") },
                                selected = currentRoute == "inventory",
                                onClick = { navController.navigate("inventory") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Market") },
                                label = { Text("Market") },
                                selected = currentRoute == "shopping",
                                onClick = { navController.navigate("shopping") }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            // HomeScreen'e hem durumu (isDarkTheme) hem de değiştirme fonksiyonunu gönderiyoruz
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToInventory = { navController.navigate("inventory") },
                                isDarkTheme = isDarkTheme,
                                onThemeChanged = { isDarkTheme = !isDarkTheme }
                            )
                        }
                        composable("recipes") { RecipeScreen(viewModel) }
                        composable("inventory") { InventoryScreen(viewModel) }
                        composable("shopping") { ShoppingScreen(viewModel) }
                    }
                }
            }
        }
    }
}