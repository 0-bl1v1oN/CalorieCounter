package com.maks.caloriecounter.domain.model

data class MealEntryDetails(
    val entry: MealEntry,
    val name: String,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
)
