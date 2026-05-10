package com.maks.caloriecounter.domain.util

import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.Product

object NutritionCalculator {
    fun forEntry(entry: MealEntry, product: Product): MealEntryDetails {
        val multiplier = entry.grams / 100.0
        return MealEntryDetails(
            entry = entry,
            product = product,
            calories = product.caloriesPer100g * multiplier,
            protein = product.proteinPer100g * multiplier,
            fat = product.fatPer100g * multiplier,
            carbs = product.carbsPer100g * multiplier,
        )
    }

    fun summarize(date: String, entries: List<MealEntryDetails>): DailySummary = DailySummary(
        date = date,
        calories = entries.sumOf { it.calories },
        protein = entries.sumOf { it.protein },
        fat = entries.sumOf { it.fat },
        carbs = entries.sumOf { it.carbs },
    )
}
