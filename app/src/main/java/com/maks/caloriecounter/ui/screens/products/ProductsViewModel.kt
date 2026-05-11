package com.maks.caloriecounter.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.ui.screens.addmeal.asDouble
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val date: String,
    private val productRepository: ProductRepository,
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val screenState = MutableStateFlow(ProductsUiState())

    val uiState: StateFlow<ProductsUiState> = combine(productRepository.observeProducts(), screenState) { products, state ->
        val sortedProducts = products.sortedAlphabetically()
        val favoriteProducts = sortedProducts.filter { it.isFavorite }
        val recentProducts = sortedProducts.filter { it.lastUsedAt != null }.sortedByDescending { it.lastUsedAt }
        val source = when (state.selectedFilter) {
            ProductFilter.All -> sortedProducts
            ProductFilter.Favorites -> favoriteProducts
            ProductFilter.Recent -> recentProducts
        }
        val filteredProducts = source.filterByQuery(state.searchQuery)

    state.copy(
            products = sortedProducts,
            favoriteProducts = favoriteProducts.filterByQuery(state.searchQuery),
            recentProducts = recentProducts.filterByQuery(state.searchQuery),
            filteredProducts = filteredProducts,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun updateSearchQuery(value: String) = screenState.update { it.copy(searchQuery = value) }

    fun selectFilter(filter: ProductFilter) = screenState.update { it.copy(selectedFilter = filter) }

    fun openActions(product: Product) = screenState.update { it.copy(actionsProduct = product) }

    fun closeActions() = screenState.update { it.copy(actionsProduct = null) }

    fun requestDeleteProduct(product: Product) = screenState.update { it.copy(actionsProduct = null, deleteConfirmationProduct = product) }

    fun dismissDeleteConfirmation() = screenState.update { it.copy(deleteConfirmationProduct = null) }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        productRepository.deleteProduct(product)
        screenState.update { it.copy(deleteConfirmationProduct = null, snackbarMessage = "Продукт удалён") }
    }

    fun toggleFavorite(product: Product) = viewModelScope.launch {
        productRepository.toggleFavorite(product)
        screenState.update { it.copy(actionsProduct = null) }
    }

    fun openQuickAdd(product: Product) = screenState.update { it.copy(actionsProduct = null, quickAdd = QuickAddState(product = product), errorMessage = null) }

    fun closeQuickAdd() = screenState.update { it.copy(quickAdd = QuickAddState(), errorMessage = null) }

    fun updateQuickGrams(value: String) = screenState.update { it.copy(quickAdd = it.quickAdd.copy(grams = value), errorMessage = null) }

    fun updateQuickMealType(value: MealType) = screenState.update { it.copy(quickAdd = it.quickAdd.copy(mealType = value)) }

    fun quickAddProduct() {
        val quick = screenState.value.quickAdd
        val product = quick.product ?: return
        val grams = quick.grams.asDouble()
        if (grams == null || grams <= 0) {
            screenState.update { it.copy(errorMessage = "Граммы должны быть больше 0") }
            return
        }
        viewModelScope.launch {
            mealRepository.addProductToDate(product, date, grams, quick.mealType)
            productRepository.updateLastUsedAt(product.id)
            screenState.update { it.copy(quickAdd = QuickAddState(), errorMessage = null, snackbarMessage = "Добавлено в дневник") }
        }
    }

    fun startAddProduct(scannedBarcode: String? = null, barcodeFormat: String? = null, initialForm: ProductFormState? = null) = screenState.update {
        it.copy(
            form = (initialForm ?: ProductFormState()).copy(
                barcode = scannedBarcode?.takeIf { barcode -> barcode.isNotBlank() },
                barcodeFormat = barcodeFormat,
            ),
            editingProductId = null,
            errorMessage = null,
            isFormSaved = false,
            snackbarMessage = if (scannedBarcode.isNullOrBlank()) null else "Штрихкод привязан к продукту",
        )
    }

    fun loadProductForEdit(productId: Long) {
        if (screenState.value.editingProductId == productId) return
        viewModelScope.launch {
            val product = productRepository.getProduct(productId)
            if (product == null) {
                screenState.update { it.copy(errorMessage = "Продукт не найден") }
                return@launch
            }
            screenState.update {
                it.copy(
                    form = product.toFormState(),
                    editingProductId = product.id,
                    errorMessage = null,
                    isFormSaved = false,
                )
            }
        }
    }

    fun updateFormName(value: String) = screenState.update { it.copy(form = it.form.copy(name = value), errorMessage = null) }
    fun updateFormCalories(value: String) = screenState.update { it.copy(form = it.form.copy(calories = value), errorMessage = null) }
    fun updateFormProtein(value: String) = screenState.update { it.copy(form = it.form.copy(protein = value), errorMessage = null) }
    fun updateFormFat(value: String) = screenState.update { it.copy(form = it.form.copy(fat = value), errorMessage = null) }
    fun updateFormCarbs(value: String) = screenState.update { it.copy(form = it.form.copy(carbs = value), errorMessage = null) }

    fun saveProduct() {
        val state = screenState.value
        val form = state.form
        val product = form.toProductOrNull() ?: run {
            screenState.update { it.copy(errorMessage = validateMessage(form)) }
            return
        }
        viewModelScope.launch {
            if (state.editingProductId == null) {
                productRepository.upsertProductByName(product)
            } else {
                val current = productRepository.getProduct(state.editingProductId)
                val productWithSameName = productRepository.findProductByName(product.name)
                if (productWithSameName != null && productWithSameName.id != state.editingProductId) {
                    screenState.update { it.copy(errorMessage = "Продукт с таким названием уже есть") }
                    return@launch
                }
                productRepository.updateProduct(
                    product.copy(
                        id = state.editingProductId,
                        createdAt = current?.createdAt ?: product.createdAt,
                        isFavorite = current?.isFavorite ?: false,
                        lastUsedAt = current?.lastUsedAt,
                        barcode = product.barcode ?: current?.barcode,
                        barcodeFormat = product.barcodeFormat ?: current?.barcodeFormat,
                        source = current?.source ?: product.source,
                    ),
                )
            }
            screenState.update { it.copy(isFormSaved = true, errorMessage = null, snackbarMessage = "Продукт сохранён") }
        }
    }

    fun clearSnackbarMessage() = screenState.update { it.copy(snackbarMessage = null) }

    fun clearFormSaved() = screenState.update { it.copy(isFormSaved = false) }

    private fun ProductFormState.toProductOrNull(): Product? {
        val caloriesValue = calories.asDouble()
        val proteinValue = protein.asDouble()
        val fatValue = fat.asDouble()
        val carbsValue = carbs.asDouble()
        if (name.isBlank() || caloriesValue == null || proteinValue == null || fatValue == null || carbsValue == null) return null
        if (caloriesValue < 0 || proteinValue < 0 || fatValue < 0 || carbsValue < 0) return null
        return Product(
            id = id,
            name = name.trim(),
            caloriesPer100g = caloriesValue,
            proteinPer100g = proteinValue,
            fatPer100g = fatValue,
            carbsPer100g = carbsValue,
            barcode = barcode,
            barcodeFormat = barcodeFormat,
            source = source,
        )
    }

    private fun validateMessage(form: ProductFormState): String = when {
        form.name.isBlank() -> "Название продукта не должно быть пустым"
        form.calories.asDouble() == null || form.calories.asDouble()!! < 0 -> "Калории не должны быть отрицательными"
        form.protein.asDouble() == null || form.protein.asDouble()!! < 0 -> "Белки не должны быть отрицательными"
        form.fat.asDouble() == null || form.fat.asDouble()!! < 0 -> "Жиры не должны быть отрицательными"
        else -> "Углеводы не должны быть отрицательными"
    }

    private companion object {
        val RussianCollator: Collator = Collator.getInstance(Locale("ru")).apply { strength = Collator.PRIMARY }

        fun List<Product>.sortedAlphabetically(): List<Product> = sortedWith { first, second ->
            RussianCollator.compare(first.name.normalizedForSort(), second.name.normalizedForSort())
        }

        fun List<Product>.filterByQuery(query: String): List<Product> {
            val normalizedQuery = query.trim().normalizedForSort()
            if (normalizedQuery.isBlank()) return this
            return filter { product -> product.name.normalizedForSort().contains(normalizedQuery, ignoreCase = true) }
        }

        fun String.normalizedForSort(): String = lowercase(Locale("ru")).replace('ё', 'е')
    }
}

fun Double.clean(): String = if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.1f", this)

private fun Product.toFormState(): ProductFormState = ProductFormState(
    id = id,
    name = name,
    calories = caloriesPer100g.clean(),
    protein = proteinPer100g.clean(),
    fat = fatPer100g.clean(),
    carbs = carbsPer100g.clean(),
    barcode = barcode,
    barcodeFormat = barcodeFormat,
    source = source,
)
