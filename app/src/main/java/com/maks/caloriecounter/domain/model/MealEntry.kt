package com.maks.caloriecounter.domain.model

data class MealEntry(
    val id: Long = 0,
    val type: FoodLogEntryType = FoodLogEntryType.Product,
    val sourceId: Long,
    val productId: Long? = if (type == FoodLogEntryType.Product) sourceId else null,
    val date: String,
    val grams: Double,
    val amountUnit: String = "g",
    val mealType: MealType,
    val nameSnapshot: String,
    val caloriesSnapshot: Double,
    val proteinSnapshot: Double,
    val fatSnapshot: Double,
    val carbsSnapshot: Double,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val amount: Double get() = grams
}