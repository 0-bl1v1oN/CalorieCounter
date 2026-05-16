package com.maks.caloriecounter.domain.model

data class DishIngredient(
    val id: Long = 0,
    val productId: Long,
    val productSnapshot: ProductSnapshot,
    val grams: Double,
)

data class Dish(
    val id: Long = 0,
    val name: String,
    val ingredients: List<DishIngredient>,
    val totalWeight: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)
