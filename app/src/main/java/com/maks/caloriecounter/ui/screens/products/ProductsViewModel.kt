package com.maks.caloriecounter.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.ui.screens.addmeal.asDouble
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
    private val formState = MutableStateFlow(ProductsUiState())

    val uiState: StateFlow<ProductsUiState> = combine(productRepository.observeProducts(), formState) { products, state ->
        state.copy(products = products.filter { it.name.contains(state.query, ignoreCase = true) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun updateQuery(value: String) = formState.update { it.copy(query = value) }
    fun updateFormName(value: String) = formState.update { it.copy(form = it.form.copy(name = value), error = null) }
    fun updateFormCalories(value: String) = formState.update { it.copy(form = it.form.copy(calories = value), error = null) }
    fun updateFormProtein(value: String) = formState.update { it.copy(form = it.form.copy(protein = value), error = null) }
    fun updateFormFat(value: String) = formState.update { it.copy(form = it.form.copy(fat = value), error = null) }
    fun updateFormCarbs(value: String) = formState.update { it.copy(form = it.form.copy(carbs = value), error = null) }

    fun edit(product: Product) = formState.update {
        it.copy(
            editingProductId = product.id,
            form = ProductFormState(product.id, product.name, product.caloriesPer100g.clean(), product.proteinPer100g.clean(), product.fatPer100g.clean(), product.carbsPer100g.clean()),
            error = null,
        )
    }

    fun cancelEdit() = formState.update { it.copy(editingProductId = null, form = ProductFormState(), error = null) }

    fun saveProduct() {
        val state = formState.value
        val form = state.form
        val product = form.toProductOrNull() ?: run {
            formState.update { it.copy(error = validateMessage(form)) }
            return
        }
        viewModelScope.launch {
            if (state.editingProductId == null) productRepository.upsertProductByName(product) else productRepository.updateProduct(product.copy(id = state.editingProductId))
            cancelEdit()
        }
    }

    fun delete(product: Product) = viewModelScope.launch { productRepository.deleteProduct(product) }

    fun openQuickAdd(product: Product) = formState.update { it.copy(quickAdd = QuickAddState(product = product)) }
    fun closeQuickAdd() = formState.update { it.copy(quickAdd = QuickAddState(), error = null) }
    fun updateQuickGrams(value: String) = formState.update { it.copy(quickAdd = it.quickAdd.copy(grams = value), error = null) }
    fun updateQuickMealType(value: MealType) = formState.update { it.copy(quickAdd = it.quickAdd.copy(mealType = value)) }
    fun quickAdd() {
        val quick = formState.value.quickAdd
        val product = quick.product ?: return
        val grams = quick.grams.asDouble()
        if (grams == null || grams <= 0) {
            formState.update { it.copy(error = "Граммы должны быть больше 0") }
            return
        }
        viewModelScope.launch {
            mealRepository.addProductToDate(product, date, grams, quick.mealType)
            closeQuickAdd()
        }
    }

    private fun ProductFormState.toProductOrNull(): Product? {
        val caloriesValue = calories.asDouble()
        val proteinValue = protein.asDouble()
        val fatValue = fat.asDouble()
        val carbsValue = carbs.asDouble()
        if (name.isBlank() || caloriesValue == null || proteinValue == null || fatValue == null || carbsValue == null) return null
        if (caloriesValue < 0 || proteinValue < 0 || fatValue < 0 || carbsValue < 0) return null
        return Product(id = id, name = name.trim(), caloriesPer100g = caloriesValue, proteinPer100g = proteinValue, fatPer100g = fatValue, carbsPer100g = carbsValue)
    }

    private fun validateMessage(form: ProductFormState): String = when {
        form.name.isBlank() -> "Название продукта не должно быть пустым"
        form.calories.asDouble() == null || form.calories.asDouble()!! < 0 -> "Калории не должны быть отрицательными"
        form.protein.asDouble() == null || form.protein.asDouble()!! < 0 -> "Белки не должны быть отрицательными"
        form.fat.asDouble() == null || form.fat.asDouble()!! < 0 -> "Жиры не должны быть отрицательными"
        else -> "Углеводы не должны быть отрицательными"
    }
}

fun Double.clean(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
