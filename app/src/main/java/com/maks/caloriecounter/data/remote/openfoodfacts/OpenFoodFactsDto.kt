package com.maks.caloriecounter.data.remote.openfoodfacts

import com.google.gson.annotations.SerializedName

data class OpenFoodFactsResponseDto(
    val code: String? = null,
    val status: Int? = null,
    @SerializedName("status_verbose") val statusVerbose: String? = null,
    val product: OpenFoodFactsProductDto? = null,
)

data class OpenFoodFactsProductDto(
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_name_ru") val productNameRu: String? = null,
    val brands: String? = null,
    val nutriments: OpenFoodFactsNutrimentsDto? = null,
)

data class OpenFoodFactsNutrimentsDto(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerializedName("proteins_100g") val proteins100g: Double? = null,
    @SerializedName("fat_100g") val fat100g: Double? = null,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Double? = null,
)
