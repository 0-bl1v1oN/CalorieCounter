package com.maks.caloriecounter.ui.screens.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.DishRepository
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.ui.screens.addmeal.asDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DishLogViewModel(
    private val date: String,
    dishId: Long,
    private val dishRepository: DishRepository,
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DishLogUiState())
    val uiState: StateFlow<DishLogUiState> = _uiState

    init {
        viewModelScope.launch {
            val dish = dishRepository.getDish(dishId)
            _uiState.update { it.copy(dish = dish, grams = dish?.totalWeight?.let { grams -> if (grams % 1.0 == 0.0) grams.toInt().toString() else grams.toString() }.orEmpty()) }
        }
    }

    fun updateGrams(value: String) = _uiState.update { it.copy(grams = value, error = null) }
    fun updateMealType(value: MealType) = _uiState.update { it.copy(mealType = value) }

    fun toggleFavorite() {
        val dish = _uiState.value.dish ?: return
        viewModelScope.launch {
            dishRepository.toggleFavorite(dish)
            _uiState.update { it.copy(dish = dish.copy(isFavorite = !dish.isFavorite)) }
        }
    }
    
    fun save() {
        val state = _uiState.value
        val dish = state.dish ?: return
        val grams = state.grams.asDouble()
        if (grams == null || grams <= 0.0) {
            _uiState.update { it.copy(error = "Граммы должны быть больше 0") }
            return
        }
        viewModelScope.launch {
            mealRepository.addDishToDate(dish, date, grams, state.mealType)
            dishRepository.updateLastUsedAt(dish.id)
            _uiState.update { it.copy(saved = true, error = null) }
        }
    }
}
