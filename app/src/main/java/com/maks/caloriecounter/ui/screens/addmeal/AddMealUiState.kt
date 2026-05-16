package com.maks.caloriecounter.ui.screens.addmeal

import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsProduct
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product

enum class AddMealProductFilter(val title: String) {
    Products("Продукты"),
    Dishes("Блюда"),
    Favorites("Избранные"),
    Recent("Недавние"),
}

data class AddMealListItem(
    val product: Product? = null,
    val dish: Dish? = null,
    val showTypeBadge: Boolean = false,
) {
    val name: String = product?.name ?: dish?.name.orEmpty()
}

data class PendingScannedBarcode(
    val rawValue: String,
    val format: String?,
    val candidates: List<String> = listOf(rawValue),
)

data class AddMealUiState(
    val searchQuery: String = "",
    val selectedFilter: AddMealProductFilter = AddMealProductFilter.Products,
    val products: List<Product> = emptyList(),
    val dishes: List<Dish> = emptyList(),
    val filteredItems: List<AddMealListItem> = emptyList(),
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
