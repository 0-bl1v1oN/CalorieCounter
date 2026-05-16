package com.maks.caloriecounter.domain.model

data class ProductSnapshot(
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
)

fun Product.toSnapshot(): ProductSnapshot = ProductSnapshot(
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
)
