package com.maks.caloriecounter.data.remote.openfoodfacts

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = FIELDS,
    ): OpenFoodFactsResponseDto

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
        private const val FIELDS = "code,status,status_verbose,product_name,product_name_ru,brands,nutriments"
    }
}
