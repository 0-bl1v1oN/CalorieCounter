package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.dao.DishDao
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.util.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DishRepository(private val dishDao: DishDao) {
    fun observeDishes(): Flow<List<Dish>> = dishDao.observeDishes().map { dishes -> dishes.map { it.toDomain() } }

    suspend fun getDish(id: Long): Dish? = dishDao.getDishById(id)?.toDomain()

    suspend fun saveDish(dish: Dish): Long {
        val calculated = dish.recalculate()
        return if (calculated.id == 0L) {
            dishDao.insertDishWithIngredients(calculated.toEntity(), calculated.ingredients.map { it.toEntity(0) })
        } else {
            dishDao.updateDishWithIngredients(calculated.toEntity(), calculated.ingredients.map { it.toEntity(calculated.id) })
            calculated.id
        }
    }

    suspend fun deleteDish(dish: Dish) = dishDao.deleteDishById(dish.id)

    suspend fun toggleFavorite(dish: Dish) = dishDao.updateFavorite(dish.id, !dish.isFavorite)

    suspend fun updateLastUsedAt(dishId: Long, lastUsedAt: Long = System.currentTimeMillis()) = dishDao.updateLastUsedAt(dishId, lastUsedAt)
}

fun Dish.recalculate(): Dish {
    val nutrition = NutritionCalculator.dishNutrition(ingredients)
    return copy(
        totalWeight = ingredients.sumOf(DishIngredient::grams),
        calories = nutrition.calories,
        protein = nutrition.protein,
        fat = nutrition.fat,
        carbs = nutrition.carbs,
        updatedAt = System.currentTimeMillis(),
    )
}
