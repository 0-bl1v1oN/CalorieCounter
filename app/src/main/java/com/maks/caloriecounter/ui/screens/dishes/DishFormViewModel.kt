package com.maks.caloriecounter.ui.screens.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.DishRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.model.toSnapshot
import com.maks.caloriecounter.domain.util.NutritionCalculator
import com.maks.caloriecounter.ui.screens.addmeal.asDouble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DishFormViewModel(
    private val dishId: Long?,
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DishFormUiState(isLoading = dishId != null))
    val uiState: StateFlow<DishFormUiState> = _uiState

    init {
        val editingDishId = dishId
        if (editingDishId != null) {
            viewModelScope.launch {
                val dish = dishRepository.getDish(editingDishId)
                if (dish == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Блюдо не найдено") }
                } else {
                    _uiState.update {
                        it.copy(
                            name = dish.name,
                            ingredients = dish.ingredients,
                            totalWeight = dish.totalWeight,
                            nutrition = NutritionCalculator.dishNutrition(dish.ingredients),
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, error = null) }

    fun updateIngredientGrams(ingredientId: Long, productId: Long, value: String) {
        val grams = value.asDouble() ?: 0.0
        recalculate(_uiState.value.ingredients.map { ingredient ->
            if (ingredient.id == ingredientId && ingredient.productId == productId) ingredient.copy(grams = grams) else ingredient
        })
    }

    fun removeIngredient(ingredient: DishIngredient) = recalculate(
        _uiState.value.ingredients.filterNot { it.id == ingredient.id && it.productId == ingredient.productId },
    )

    fun addProductsByIds(productIds: List<Long>) {
        if (productIds.isEmpty()) return
        viewModelScope.launch {
            val currentProductIds = _uiState.value.ingredients.map { it.productId }.toSet()
            val newIngredients = productIds
                .filterNot { it in currentProductIds }
                .mapNotNull { productRepository.getProduct(it) }
                .map { product ->
                    DishIngredient(
                        id = -System.nanoTime(),
                        productId = product.id,
                        productSnapshot = product.toSnapshot(),
                        grams = 100.0,
                    )
                }
            recalculate(_uiState.value.ingredients + newIngredients)
        }
    }

    fun save() {
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Введите название блюда") }
            return
        }
        if (state.ingredients.isEmpty()) {
            _uiState.update { it.copy(error = "Добавьте хотя бы один ингредиент") }
            return
        }
        if (state.ingredients.any { it.grams <= 0.0 }) {
            _uiState.update { it.copy(error = "Граммы ингредиентов должны быть больше 0") }
            return
        }
        viewModelScope.launch {
            val existing = dishId?.let { dishRepository.getDish(it) }
            val now = System.currentTimeMillis()
            dishRepository.saveDish(
                Dish(
                    id = dishId ?: 0L,
                    name = name,
                    ingredients = state.ingredients.map { it.copy(id = it.id.coerceAtLeast(0L)) },
                    totalWeight = state.totalWeight,
                    calories = state.nutrition.calories,
                    protein = state.nutrition.protein,
                    fat = state.nutrition.fat,
                    carbs = state.nutrition.carbs,
                    isFavorite = existing?.isFavorite ?: false,
                    lastUsedAt = existing?.lastUsedAt,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
            _uiState.update { it.copy(saved = true, error = null) }
        }
    }

    fun clearSaved() = _uiState.update { it.copy(saved = false) }

    private fun recalculate(ingredients: List<DishIngredient>) {
        val nutrition = NutritionCalculator.dishNutrition(ingredients)
        _uiState.update {
            it.copy(
                ingredients = ingredients,
                totalWeight = ingredients.sumOf(DishIngredient::grams),
                nutrition = nutrition,
                error = null,
            )
        }
    }
}
