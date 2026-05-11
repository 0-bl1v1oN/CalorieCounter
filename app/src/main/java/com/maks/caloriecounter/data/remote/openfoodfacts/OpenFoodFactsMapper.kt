package com.maks.caloriecounter.data.remote.openfoodfacts

import com.maks.caloriecounter.domain.model.Product

const val OPEN_FOOD_FACTS_SOURCE = "open_food_facts"

data class OpenFoodFactsProduct(
    val barcode: String,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val brand: String?,
)

data class OpenFoodFactsIncompleteProduct(
    val barcode: String,
    val name: String,
    val brand: String?,
)

sealed interface OpenFoodFactsLookupResult {
    data class Found(val product: OpenFoodFactsProduct) : OpenFoodFactsLookupResult
    data class Incomplete(val product: OpenFoodFactsIncompleteProduct) : OpenFoodFactsLookupResult
    data object NotFound : OpenFoodFactsLookupResult
}

fun OpenFoodFactsResponseDto.toLookupResult(requestedBarcode: String): OpenFoodFactsLookupResult {
    if (status != 1 || product == null) return OpenFoodFactsLookupResult.NotFound

    val name = listOf(product.productNameRu, product.productName, product.brands)
        .firstOrNull { !it.isNullOrBlank() }
        ?.trim()
        ?: "Продукт без названия"
    val brand = product.brands?.takeIf { it.isNotBlank() }?.trim()
    val nutriments = product.nutriments
    val calories = nutriments?.energyKcal100g
    val protein = nutriments?.proteins100g
    val fat = nutriments?.fat100g
    val carbs = nutriments?.carbohydrates100g

    return if (calories == null || protein == null || fat == null || carbs == null) {
        OpenFoodFactsLookupResult.Incomplete(OpenFoodFactsIncompleteProduct(requestedBarcode, name, brand))
    } else {
        OpenFoodFactsLookupResult.Found(
            OpenFoodFactsProduct(
                barcode = requestedBarcode,
                name = name,
                caloriesPer100g = calories,
                proteinPer100g = protein,
                fatPer100g = fat,
                carbsPer100g = carbs,
                brand = brand,
            ),
        )
    }
}

fun OpenFoodFactsProduct.toProduct(rawBarcode: String, barcodeFormat: String?): Product = Product(
    name = name,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    fatPer100g = fatPer100g,
    carbsPer100g = carbsPer100g,
    barcode = rawBarcode,
    barcodeFormat = barcodeFormat,
    source = OPEN_FOOD_FACTS_SOURCE,
    lastUsedAt = System.currentTimeMillis(),
)
