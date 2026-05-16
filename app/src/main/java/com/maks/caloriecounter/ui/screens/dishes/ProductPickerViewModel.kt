package com.maks.caloriecounter.ui.screens.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.ProductRepository
import java.text.Collator
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProductPickerViewModel(productRepository: ProductRepository) : ViewModel() {
    private val state = MutableStateFlow(ProductPickerUiState())
    val uiState: StateFlow<ProductPickerUiState> = combine(productRepository.observeProducts(), state) { products, current ->
        val query = current.searchQuery.trim().normalized()
        val sorted = products.sortedWith { a, b -> collator.compare(a.name.normalized(), b.name.normalized()) }
        current.copy(products = if (query.isBlank()) sorted else sorted.filter { it.name.normalized().contains(query, ignoreCase = true) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductPickerUiState())

    fun updateSearchQuery(value: String) = state.update { it.copy(searchQuery = value) }
    fun toggleProduct(productId: Long) = state.update {
        it.copy(selectedIds = if (productId in it.selectedIds) it.selectedIds - productId else it.selectedIds + productId)
    }

    private companion object {
        val collator: Collator = Collator.getInstance(Locale("ru")).apply { strength = Collator.PRIMARY }
        fun String.normalized(): String = lowercase(Locale("ru")).replace('ё', 'е')
    }
}
