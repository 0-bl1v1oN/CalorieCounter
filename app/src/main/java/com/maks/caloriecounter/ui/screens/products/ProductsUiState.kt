package com.maks.caloriecounter.ui.screens.products

import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product

enum class ProductFilter(val title: String) {
    All("Все"),
    Favorites("Избранные"),
    Recent("Недавние"),
}

data class ProductFormState(
    val id: Long = 0,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
)

data class QuickAddState(
    val product: Product? = null,
    val grams: String = "",
    val mealType: MealType = MealType.Breakfast,
)

data class ProductsUiState(
    val searchQuery: String = "",
    val selectedFilter: ProductFilter = ProductFilter.All,
    val products: List<Product> = emptyList(),
    val favoriteProducts: List<Product> = emptyList(),
    val recentProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val quickAdd: QuickAddState = QuickAddState(),
    val actionsProduct: Product? = null,
    val deleteConfirmationProduct: Product? = null,
    val form: ProductFormState = ProductFormState(),
    val editingProductId: Long? = null,
    val isFormSaved: Boolean = false,
)
