package com.maks.caloriecounter.ui.screens.dishes

import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.util.MealNutrition

data class DishFormUiState(
    val name: String = "",
    val ingredients: List<DishIngredient> = emptyList(),
    val totalWeight: Double = 0.0,
    val nutrition: MealNutrition = MealNutrition(0.0, 0.0, 0.0, 0.0),
    val error: String? = null,
    val saved: Boolean = false,
    val isLoading: Boolean = false,
)

data class ProductPickerUiState(
    val searchQuery: String = "",
    val products: List<Product> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
)

data class DishLogUiState(
    val dish: Dish? = null,
    val grams: String = "",
    val mealType: com.maks.caloriecounter.domain.model.MealType = com.maks.caloriecounter.domain.model.MealType.Breakfast,
    val error: String? = null,
    val saved: Boolean = false,
)
