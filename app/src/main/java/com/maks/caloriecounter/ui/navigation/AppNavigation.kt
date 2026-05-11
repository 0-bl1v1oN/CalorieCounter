package com.maks.caloriecounter.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import com.maks.caloriecounter.di.AppContainer
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.screens.addmeal.AddMealScreen
import com.maks.caloriecounter.ui.screens.history.HistoryScreen
import com.maks.caloriecounter.ui.screens.products.ProductFormScreen
import com.maks.caloriecounter.ui.screens.products.ProductFormState
import com.maks.caloriecounter.ui.screens.products.ProductsScreen
import com.maks.caloriecounter.ui.screens.settings.SettingsScreen
import com.maks.caloriecounter.ui.screens.today.TodayScreen

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    var selectedDate by rememberSaveable { mutableStateOf(DateUtils.today()) }
    var todaySnackbarMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Routes.Today, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.Today) {
                val vm: com.maks.caloriecounter.ui.screens.today.TodayViewModel = viewModel(key = "today-$selectedDate", factory = appContainer.todayViewModelFactory(selectedDate))
                TodayScreen(
                    viewModel = vm,
                    onPreviousDay = { selectedDate = DateUtils.shift(selectedDate, -1) },
                    onNextDay = { selectedDate = DateUtils.shift(selectedDate, 1) },
                    onAddMeal = { navController.navigate(Routes.AddMeal) },
                    snackbarMessage = todaySnackbarMessage,
                    onSnackbarShown = { todaySnackbarMessage = null },
                )
            }
            composable(Routes.AddMeal) {
                val vm: com.maks.caloriecounter.ui.screens.addmeal.AddMealViewModel = viewModel(key = "add-$selectedDate", factory = appContainer.addMealViewModelFactory(selectedDate))
                AddMealScreen(
                    viewModel = vm,
                    onSaved = {
                        todaySnackbarMessage = "Добавлено в дневник"
                        navController.popBackStack()
                    },
                    onCreateProduct = { barcode, format, product -> navController.navigate(if (product == null) Routes.productAdd(barcode, format) else Routes.productAdd(barcode, format, product)) },
                )
            }
            composable(Routes.Products) {
                val vm: com.maks.caloriecounter.ui.screens.products.ProductsViewModel = viewModel(key = "products-$selectedDate", factory = appContainer.productsViewModelFactory(selectedDate))
                ProductsScreen(
                    viewModel = vm,
                    onAddProduct = { navController.navigate(Routes.ProductAddBase) },
                    onEditProduct = { productId -> navController.navigate(Routes.productEdit(productId)) },
                )
            }
            composable(
                route = Routes.ProductAdd,
                arguments = listOf(
                    navArgument("scannedBarcode") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("barcodeFormat") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("name") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("calories") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("protein") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("fat") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("carbs") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("source") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { entry ->
                val scannedBarcode = entry.arguments?.getString("scannedBarcode")?.let(Uri::decode)?.takeIf { it.isNotBlank() }
                val barcodeFormat = entry.arguments?.getString("barcodeFormat")?.let(Uri::decode)?.takeIf { it.isNotBlank() }
                val initialForm = ProductFormState(
                    name = entry.arguments?.getString("name")?.let(Uri::decode).orEmpty(),
                    calories = entry.arguments?.getString("calories")?.let(Uri::decode).orEmpty(),
                    protein = entry.arguments?.getString("protein")?.let(Uri::decode).orEmpty(),
                    fat = entry.arguments?.getString("fat")?.let(Uri::decode).orEmpty(),
                    carbs = entry.arguments?.getString("carbs")?.let(Uri::decode).orEmpty(),
                    source = entry.arguments?.getString("source")?.let(Uri::decode)?.takeIf { it.isNotBlank() } ?: "manual",
                ).takeIf { it.name.isNotBlank() || it.calories.isNotBlank() || it.protein.isNotBlank() || it.fat.isNotBlank() || it.carbs.isNotBlank() }
                val vm: com.maks.caloriecounter.ui.screens.products.ProductsViewModel = viewModel(key = "product-add-$selectedDate-${scannedBarcode.orEmpty()}", factory = appContainer.productsViewModelFactory(selectedDate))
                ProductFormScreen(
                    vm,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                    scannedBarcode = scannedBarcode,
                    barcodeFormat = barcodeFormat,
                    initialForm = initialForm,
                )
            }
            composable(
                route = Routes.ProductEdit,
                arguments = listOf(navArgument("productId") { type = NavType.LongType }),
            ) { entry ->
                val productId = requireNotNull(entry.arguments?.getLong("productId"))
                val vm: com.maks.caloriecounter.ui.screens.products.ProductsViewModel = viewModel(key = "product-edit-$productId", factory = appContainer.productsViewModelFactory(selectedDate))
                ProductFormScreen(vm, onSaved = { navController.popBackStack() }, onCancel = { navController.popBackStack() }, productId = productId)
            }
            composable(Routes.History) {
                val vm: com.maks.caloriecounter.ui.screens.history.HistoryViewModel = viewModel(factory = appContainer.historyViewModelFactory())
                HistoryScreen(vm, onDateClick = { date -> selectedDate = date; navController.navigate(Routes.Today) })
            }
            composable(Routes.Settings) {
                val vm: com.maks.caloriecounter.ui.screens.settings.SettingsViewModel = viewModel(factory = appContainer.settingsViewModelFactory())
                SettingsScreen(vm)
            }
        }
    }
}
