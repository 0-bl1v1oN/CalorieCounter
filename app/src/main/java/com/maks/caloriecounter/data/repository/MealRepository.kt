package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.dao.MealEntryDao
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.util.NutritionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealRepository(private val mealEntryDao: MealEntryDao) {
    fun observeEntriesForDate(date: String): Flow<List<MealEntryDetails>> =
        mealEntryDao.observeEntriesForDate(date).map { entries -> entries.map { it.toDomain() } }

    fun observeHistory(): Flow<List<DailySummary>> = mealEntryDao.observeAllEntries().map { entries ->
        entries.map { it.toDomain() }
            .groupBy { it.entry.date }
            .map { (date, dateEntries) -> NutritionCalculator.summarize(date, dateEntries) }
            .sortedByDescending { it.date }
    }

    suspend fun insertEntry(entry: MealEntry): Long = mealEntryDao.insertEntry(entry.toEntity())

    suspend fun updateEntryGrams(entryId: Long, grams: Double) {
        val entry = mealEntryDao.getEntryById(entryId) ?: return
        mealEntryDao.updateEntry(entry.copy(grams = grams))
    }

    suspend fun deleteEntry(entry: MealEntry) = mealEntryDao.deleteEntry(entry.toEntity())

    suspend fun addProductToDate(product: Product, date: String, grams: Double, mealType: com.maks.caloriecounter.domain.model.MealType): Long =
        insertEntry(MealEntry(productId = product.id, date = date, grams = grams, mealType = mealType))
}
