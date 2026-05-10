package com.maks.caloriecounter.ui.screens.products

import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product

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
    val products: List<Product> = emptyList(),
    val query: String = "",
    val form: ProductFormState = ProductFormState(),
    val editingProductId: Long? = null,
    val quickAdd: QuickAddState = QuickAddState(),
    val error: String? = null,
)
