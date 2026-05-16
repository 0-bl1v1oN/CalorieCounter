package com.maks.caloriecounter.ui.navigation

import android.net.Uri
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsProduct

object Routes {
    const val Today = "today"
    const val Products = "products"
    const val ProductAddBase = "products/add"
    const val ProductAdd = "$ProductAddBase?scannedBarcode={scannedBarcode}&barcodeFormat={barcodeFormat}&name={name}&calories={calories}&protein={protein}&fat={fat}&carbs={carbs}&source={source}"
    const val ProductEdit = "products/edit/{productId}"
    const val History = "history"
    const val Settings = "settings"
    const val AddMeal = "addMeal"
    const val DishAdd = "dishes/add"
    const val DishEdit = "dishes/edit/{dishId}"
    const val DishProductPicker = "dishes/pickProducts"
    const val DishLog = "dishes/log/{dishId}"

    fun productAdd(scannedBarcode: String?, barcodeFormat: String?): String = productAdd(
        scannedBarcode = scannedBarcode,
        barcodeFormat = barcodeFormat,
        name = null,
        calories = null,
        protein = null,
        fat = null,
        carbs = null,
        source = null,
    )

    fun productAdd(scannedBarcode: String?, barcodeFormat: String?, product: OpenFoodFactsProduct): String = productAdd(
        scannedBarcode = scannedBarcode,
        barcodeFormat = barcodeFormat,
        name = product.name,
        calories = product.caloriesPer100g.toString(),
        protein = product.proteinPer100g.toString(),
        fat = product.fatPer100g.toString(),
        carbs = product.carbsPer100g.toString(),
        source = "open_food_facts",
    )

    private fun productAdd(
        scannedBarcode: String?,
        barcodeFormat: String?,
        name: String?,
        calories: String?,
        protein: String?,
        fat: String?,
        carbs: String?,
        source: String?,
    ): String = buildString {
        append(ProductAddBase)
        append("?scannedBarcode=")
        append(scannedBarcode.encodedOrEmpty())
        append("&barcodeFormat=")
        append(barcodeFormat.encodedOrEmpty())
        append("&name=")
        append(name.encodedOrEmpty())
        append("&calories=")
        append(calories.encodedOrEmpty())
        append("&protein=")
        append(protein.encodedOrEmpty())
        append("&fat=")
        append(fat.encodedOrEmpty())
        append("&carbs=")
        append(carbs.encodedOrEmpty())
        append("&source=")
        append(source.encodedOrEmpty())
    }

    fun productEdit(productId: Long): String = "products/edit/$productId"
    fun dishEdit(dishId: Long): String = "dishes/edit/$dishId"
    fun dishLog(dishId: Long): String = "dishes/log/$dishId"
}

private fun String?.encodedOrEmpty(): String = this?.takeIf { it.isNotBlank() }?.let(Uri::encode).orEmpty()
