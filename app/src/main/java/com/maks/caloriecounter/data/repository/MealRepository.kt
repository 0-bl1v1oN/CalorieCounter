package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.dao.MealEntryDao
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.FoodLogEntryType
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.util.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealRepository(private val mealEntryDao: MealEntryDao) {
    fun observeEntriesForDate(date: String): Flow<List<MealEntryDetails>> =
        mealEntryDao.observeEntriesForDate(date).map { entries -> entries.map { it.toDetails() } }

    fun observeHistory(): Flow<List<DailySummary>> = mealEntryDao.observeAllEntries().map { entries ->
        entries.map { it.toDetails() }
            .groupBy { it.entry.date }
            .map { (date, dateEntries) -> NutritionCalculator.summarize(date, dateEntries) }
            .sortedByDescending { it.date }
    }

    suspend fun insertEntry(entry: MealEntry): Long = mealEntryDao.insertEntry(entry.toEntity())

    suspend fun updateEntryGrams(entryId: Long, grams: Double) {
        val entry = mealEntryDao.getEntryById(entryId) ?: return
        if (grams <= 0.0) return
        val ratio = if (entry.grams <= 0.0) 1.0 else grams / entry.grams
        mealEntryDao.updateEntry(
            entry.copy(
                grams = grams,
                caloriesSnapshot = entry.caloriesSnapshot * ratio,
                proteinSnapshot = entry.proteinSnapshot * ratio,
                fatSnapshot = entry.fatSnapshot * ratio,
                carbsSnapshot = entry.carbsSnapshot * ratio,
            ),
        )
    }

    suspend fun deleteEntry(entry: MealEntry) = mealEntryDao.deleteEntry(entry.toEntity())

    suspend fun addProductToDate(product: Product, date: String, grams: Double, mealType: MealType): Long {
        val nutrition = NutritionCalculator.forProduct(product, grams)
        return insertEntry(
            MealEntry(
                type = FoodLogEntryType.Product,
                sourceId = product.id,
                productId = product.id,
                date = date,
                grams = grams,
                mealType = mealType,
                nameSnapshot = product.name,
                caloriesSnapshot = nutrition.calories,
                proteinSnapshot = nutrition.protein,
                fatSnapshot = nutrition.fat,
                carbsSnapshot = nutrition.carbs,
            ),
        )
    }

    suspend fun addDishToDate(dish: Dish, date: String, grams: Double, mealType: MealType): Long {
        val nutrition = NutritionCalculator.forDishPortion(dish, grams)
        return insertEntry(
            MealEntry(
                type = FoodLogEntryType.Dish,
                sourceId = dish.id,
                productId = null,
                date = date,
                grams = grams,
                mealType = mealType,
                nameSnapshot = dish.name,
                caloriesSnapshot = nutrition.calories,
                proteinSnapshot = nutrition.protein,
                fatSnapshot = nutrition.fat,
                carbsSnapshot = nutrition.carbs,
            ),
        )
}
