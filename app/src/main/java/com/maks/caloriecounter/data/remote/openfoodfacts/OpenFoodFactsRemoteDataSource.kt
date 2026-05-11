package com.maks.caloriecounter.data.remote.openfoodfacts

import com.google.gson.JsonParseException
import java.io.IOException
import retrofit2.HttpException

class OpenFoodFactsRemoteDataSource(private val api: OpenFoodFactsApi) {
    suspend fun findProduct(barcode: String): OpenFoodFactsRemoteResult = try {
        when (val result = api.getProduct(barcode).toLookupResult(barcode)) {
            is OpenFoodFactsLookupResult.Found -> OpenFoodFactsRemoteResult.Found(result.product)
            is OpenFoodFactsLookupResult.Incomplete -> OpenFoodFactsRemoteResult.Incomplete(result.product)
            OpenFoodFactsLookupResult.NotFound -> OpenFoodFactsRemoteResult.NotFound
        }
    } catch (exception: IOException) {
        OpenFoodFactsRemoteResult.NoConnection
    } catch (exception: HttpException) {
        OpenFoodFactsRemoteResult.ApiError
    } catch (exception: JsonParseException) {
        OpenFoodFactsRemoteResult.ApiError
    }
}

sealed interface OpenFoodFactsRemoteResult {
    data class Found(val product: OpenFoodFactsProduct) : OpenFoodFactsRemoteResult
    data class Incomplete(val product: OpenFoodFactsIncompleteProduct) : OpenFoodFactsRemoteResult
    data object NotFound : OpenFoodFactsRemoteResult
    data object NoConnection : OpenFoodFactsRemoteResult
    data object ApiError : OpenFoodFactsRemoteResult
}
