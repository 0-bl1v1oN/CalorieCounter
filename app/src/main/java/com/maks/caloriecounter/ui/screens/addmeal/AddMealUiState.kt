package com.maks.caloriecounter.ui.screens.addmeal

import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsProduct
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product

enum class AddMealProductFilter(val title: String) {
    All("Все"),
    Favorites("Избранные"),
    Recent("Недавние"),
}

data class PendingScannedBarcode(
    val rawValue: String,
    val format: String?,
    val candidates: List<String> = listOf(rawValue),
)

data class AddMealUiState(
    val searchQuery: String = "",
    val selectedFilter: AddMealProductFilter = AddMealProductFilter.All,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val grams: String = "",
    val mealType: MealType = MealType.Breakfast,
    val isLoading: Boolean = true,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val pendingScannedBarcode: PendingScannedBarcode? = null,
    val isOpenFoodFactsLoading: Boolean = false,
    val openFoodFactsProduct: OpenFoodFactsProduct? = null,
    val openFoodFactsError: String? = null,
    val saved: Boolean = false,
)
