package com.maks.caloriecounter.ui.screens.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddMealViewModel(
    private val date: String,
    private val productRepository: ProductRepository,
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState

    fun updateName(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun updateGrams(value: String) = _uiState.update { it.copy(grams = value, error = null) }
    fun updateCalories(value: String) = _uiState.update { it.copy(calories = value, error = null) }
    fun updateProtein(value: String) = _uiState.update { it.copy(protein = value, error = null) }
    fun updateFat(value: String) = _uiState.update { it.copy(fat = value, error = null) }
    fun updateCarbs(value: String) = _uiState.update { it.copy(carbs = value, error = null) }
    fun updateMealType(value: MealType) = _uiState.update { it.copy(mealType = value) }

    fun save() {
        val state = uiState.value
        val name = state.name.trim()
        val grams = state.grams.asDouble()
        val calories = state.calories.asDouble()
        val protein = state.protein.asDouble()
        val fat = state.fat.asDouble()
        val carbs = state.carbs.asDouble()
        val error = validate(name, grams, calories, protein, fat, carbs)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            val productId = productRepository.upsertProductByName(
                Product(name = name, caloriesPer100g = calories!!, proteinPer100g = protein!!, fatPer100g = fat!!, carbsPer100g = carbs!!),
            )
            mealRepository.insertEntry(MealEntry(productId = productId, date = date, grams = grams!!, mealType = state.mealType))
            _uiState.update { it.copy(saved = true) }
        }
    }

    private fun validate(name: String, grams: Double?, calories: Double?, protein: Double?, fat: Double?, carbs: Double?): String? = when {
        name.isBlank() -> "Название продукта не должно быть пустым"
        grams == null || grams <= 0 -> "Граммы должны быть больше 0"
        calories == null || calories < 0 -> "Калории не должны быть отрицательными"
        protein == null || protein < 0 -> "Белки не должны быть отрицательными"
        fat == null || fat < 0 -> "Жиры не должны быть отрицательными"
        carbs == null || carbs < 0 -> "Углеводы не должны быть отрицательными"
        else -> null
    }
}

fun String.asDouble(): Double? = replace(',', '.').toDoubleOrNull()
