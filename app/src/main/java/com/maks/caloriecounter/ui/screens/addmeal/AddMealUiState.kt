package com.maks.caloriecounter.ui.screens.addmeal

import com.maks.caloriecounter.domain.model.MealType

data class AddMealUiState(
    val name: String = "",
    val grams: String = "",
    val calories: String = "",
    val protein: String = "",
    val fat: String = "",
    val carbs: String = "",
    val mealType: MealType = MealType.Breakfast,
    val error: String? = null,
    val saved: Boolean = false,
)
