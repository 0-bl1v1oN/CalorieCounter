package com.maks.caloriecounter.ui.screens.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
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
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val screenState = MutableStateFlow(AddMealUiState())

    val uiState: StateFlow<AddMealUiState> = combine(productRepository.observeProducts(), screenState) { products, state ->
        val sortedProducts = products.sortedAlphabetically()
        val source = when (state.selectedFilter) {
            AddMealProductFilter.All -> sortedProducts
            AddMealProductFilter.Favorites -> sortedProducts.filter { it.isFavorite }
            AddMealProductFilter.Recent -> sortedProducts.filter { it.lastUsedAt != null }.sortedByDescending { it.lastUsedAt }
        }
        state.copy(
            products = sortedProducts,
            filteredProducts = source.filterByQuery(state.searchQuery),
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

        fun List<Product>.filterByQuery(query: String): List<Product> {
            val normalizedQuery = query.trim().normalizedForSort()
            if (normalizedQuery.isBlank()) return this
            return filter { product -> product.name.normalizedForSort().contains(normalizedQuery, ignoreCase = true) }
        }

        fun String.normalizedForSort(): String = lowercase(Locale("ru")).replace('ё', 'е')
    }
}

fun String.asDouble(): Double? = replace(',', '.').toDoubleOrNull()
