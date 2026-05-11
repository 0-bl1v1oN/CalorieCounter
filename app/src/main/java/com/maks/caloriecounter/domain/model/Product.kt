package com.maks.caloriecounter.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val barcode: String? = null,
    val barcodeFormat: String? = null,
    val source: String = "manual",
)
