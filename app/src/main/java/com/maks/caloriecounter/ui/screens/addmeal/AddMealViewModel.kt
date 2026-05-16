package com.maks.caloriecounter.ui.screens.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsRemoteDataSource
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsRemoteResult
import com.maks.caloriecounter.data.remote.openfoodfacts.toProduct
import com.maks.caloriecounter.data.repository.DishRepository
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.util.BarcodeNormalizer
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddMealViewModel(
    private val date: String,
    private val productRepository: ProductRepository,
    private val dishRepository: DishRepository,
    private val mealRepository: MealRepository,
    private val openFoodFactsRemoteDataSource: OpenFoodFactsRemoteDataSource,
) : ViewModel() {
    private val screenState = MutableStateFlow(AddMealUiState())

    val uiState: StateFlow<AddMealUiState> = combine(
        productRepository.observeProducts(),
        dishRepository.observeDishes(),
        screenState,
    ) { products, dishes, state ->
        val sortedProducts = products.sortedAlphabetically()
        val sortedDishes = dishes.sortedAlphabetically()
        val items = when (state.selectedFilter) {
            AddMealProductFilter.Products -> sortedProducts.map { AddMealListItem(product = it) }
            AddMealProductFilter.Dishes -> sortedDishes.map { AddMealListItem(dish = it) }
            AddMealProductFilter.Favorites -> (
                sortedProducts.filter { it.isFavorite }.map { AddMealListItem(product = it, showTypeBadge = true) } +
                    sortedDishes.filter { it.isFavorite }.map { AddMealListItem(dish = it, showTypeBadge = true) }
                ).sortedByName()
            AddMealProductFilter.Recent -> (
                sortedProducts.filter { it.lastUsedAt != null }.map { AddMealListItem(product = it, showTypeBadge = true) } +
                    sortedDishes.filter { it.lastUsedAt != null }.map { AddMealListItem(dish = it, showTypeBadge = true) }
                ).sortedByDescending { it.product?.lastUsedAt ?: it.dish?.lastUsedAt ?: 0L }
        }
        state.copy(
            products = sortedProducts,
            dishes = sortedDishes,
            filteredItems = items.filterByQuery(state.searchQuery),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddMealUiState())

    fun updateSearchQuery(value: String) = screenState.update { it.copy(searchQuery = value) }

    fun selectFilter(filter: AddMealProductFilter) = screenState.update { it.copy(selectedFilter = filter) }

    fun selectProduct(product: Product) = screenState.update {
        it.copy(selectedProduct = product, grams = it.grams.ifBlank { "100" }, error = null)
    }

    fun clearSelectedProduct() = screenState.update { it.copy(selectedProduct = null, error = null) }

    fun updateGrams(value: String) = screenState.update { it.copy(grams = value, error = null) }

    fun updateMealType(value: MealType) = screenState.update { it.copy(mealType = value) }

    fun onScanCancelled() = screenState.update { it.copy(snackbarMessage = "Сканирование отменено") }

    fun onBarcodeNotRecognized() = screenState.update { it.copy(snackbarMessage = "Код не распознан") }

    fun onScannerUnavailable(message: String = "Сканер недоступен") = screenState.update { it.copy(snackbarMessage = message) }

    fun clearSnackbarMessage() = screenState.update { it.copy(snackbarMessage = null) }

    fun findScannedProduct(rawValue: String, format: String?) {
        val barcode = rawValue.trim()
        if (barcode.isBlank()) {
            onBarcodeNotRecognized()
            return
        }
        viewModelScope.launch {
            val normalizedCandidates = BarcodeNormalizer.candidates(barcode, format)
            val localCandidates = buildList {
                add(barcode)
                addAll(normalizedCandidates)
            }.distinct()
            val product = productRepository.findProductByBarcodes(localCandidates)
            if (product == null) {
                screenState.update {
                    it.copy(
                        pendingScannedBarcode = PendingScannedBarcode(rawValue = barcode, format = format, candidates = normalizedCandidates),
                        openFoodFactsProduct = null,
                        openFoodFactsError = null,
                        snackbarMessage = "Продукт не найден",
                    )
                }
            } else {
                screenState.update {
                    it.copy(
                        selectedProduct = product,
                        grams = it.grams.ifBlank { "100" },
                        error = null,
                        pendingScannedBarcode = null,
                        openFoodFactsProduct = null,
                        openFoodFactsError = null,
                        snackbarMessage = "Продукт найден",
                    )
                }
            }
        }
    }

    fun dismissProductNotFound() = screenState.update { it.copy(pendingScannedBarcode = null, openFoodFactsError = null) }

    fun lookupOpenFoodFacts() {
        val pending = screenState.value.pendingScannedBarcode ?: return
        if (pending.candidates.isEmpty()) {
            screenState.update { it.copy(openFoodFactsError = "Не удалось извлечь GTIN для поиска в Open Food Facts") }
            return
        }
        viewModelScope.launch {
            screenState.update { it.copy(isOpenFoodFactsLoading = true, snackbarMessage = "Ищем продукт…", openFoodFactsError = null) }
            for (candidate in pending.candidates) {
                when (val result = openFoodFactsRemoteDataSource.findProduct(candidate)) {
                    is OpenFoodFactsRemoteResult.Found -> {
                        screenState.update {
                            it.copy(
                                isOpenFoodFactsLoading = false,
                                openFoodFactsProduct = result.product,
                                openFoodFactsError = null,
                            )
                        }
                        return@launch
                    }
                    is OpenFoodFactsRemoteResult.Incomplete -> {
                        screenState.update {
                            it.copy(
                                isOpenFoodFactsLoading = false,
                                openFoodFactsProduct = null,
                                openFoodFactsError = "В Open Food Facts нет полного КБЖУ для продукта. Создайте продукт вручную или отредактируйте данные.",
                            )
                        }
                        return@launch
                    }
                    OpenFoodFactsRemoteResult.NoConnection -> {
                        screenState.update {
                            it.copy(
                                isOpenFoodFactsLoading = false,
                                openFoodFactsError = "Нет подключения к интернету",
                            )
                        }
                        return@launch
                    }
                    OpenFoodFactsRemoteResult.ApiError -> {
                        screenState.update {
                            it.copy(
                                isOpenFoodFactsLoading = false,
                                openFoodFactsError = "Не удалось получить данные продукта",
                            )
                        }
                        return@launch
                    }
                    OpenFoodFactsRemoteResult.NotFound -> Unit
                }
            }
            screenState.update {
                it.copy(
                    isOpenFoodFactsLoading = false,
                    openFoodFactsError = "Продукт не найден в Open Food Facts",
                )
            }
        }
    }

    fun dismissOpenFoodFactsProduct() = screenState.update { it.copy(openFoodFactsProduct = null) }

    fun saveOpenFoodFactsProductAndAdd() {
        val state = screenState.value
        val pending = state.pendingScannedBarcode ?: return
        val openFoodFactsProduct = state.openFoodFactsProduct ?: return
        viewModelScope.launch {
            val productId = productRepository.upsertProductByName(
                openFoodFactsProduct.toProduct(rawBarcode = pending.rawValue, barcodeFormat = pending.format),
            )
            val product = productRepository.getProduct(productId)
            if (product == null) {
                screenState.update { it.copy(openFoodFactsError = "Не удалось сохранить продукт") }
                return@launch
            }
            screenState.update {
                it.copy(
                    selectedProduct = product,
                    grams = it.grams.ifBlank { "100" },
                    pendingScannedBarcode = null,
                    openFoodFactsProduct = null,
                    openFoodFactsError = null,
                    snackbarMessage = "Продукт сохранён",
                )
            }
        }
    }
    
    fun save() {
        val state = uiState.value
        val product = state.selectedProduct ?: run {
            screenState.update { it.copy(error = "Выберите продукт") }
            return
        }
        val grams = state.grams.asDouble()
        if (grams == null || grams <= 0) {
            screenState.update { it.copy(error = "Граммы должны быть больше 0") }
            return
        }
        viewModelScope.launch {
            mealRepository.addProductToDate(product, date, grams, state.mealType)
            productRepository.updateLastUsedAt(product.id)
            screenState.update { it.copy(saved = true, error = null) }
        }
    }

    private companion object {
        val RussianCollator: Collator = Collator.getInstance(Locale("ru")).apply { strength = Collator.PRIMARY }

        fun List<Product>.sortedAlphabetically(): List<Product> = sortedWith { first, second ->
            RussianCollator.compare(first.name.normalizedForSort(), second.name.normalizedForSort())
        }

        fun List<Dish>.sortedAlphabetically(): List<Dish> = sortedWith { first, second ->
            RussianCollator.compare(first.name.normalizedForSort(), second.name.normalizedForSort())
        }

        fun List<AddMealListItem>.sortedByName(): List<AddMealListItem> = sortedWith { first, second ->
            RussianCollator.compare(first.name.normalizedForSort(), second.name.normalizedForSort())
        }

        fun List<AddMealListItem>.filterByQuery(query: String): List<AddMealListItem> {
            val normalizedQuery = query.trim().normalizedForSort()
            if (normalizedQuery.isBlank()) return this
            return filter { item -> item.name.normalizedForSort().contains(normalizedQuery, ignoreCase = true) }
        }

        fun String.normalizedForSort(): String = lowercase(Locale("ru")).replace('ё', 'е')
    }
}

fun String.asDouble(): Double? = replace(',', '.').toDoubleOrNull()
