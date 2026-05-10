package com.maks.caloriecounter.domain.model

data class MealEntry(
    val id: Long = 0,
    val productId: Long,
    val date: String,
    val grams: Double,
    val mealType: MealType,
    val createdAt: Long = System.currentTimeMillis(),
)
