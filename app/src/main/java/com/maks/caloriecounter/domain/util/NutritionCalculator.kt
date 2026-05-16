package com.maks.caloriecounter.domain.util

import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.Product

object NutritionCalculator {
    fun forProduct(product: Product, grams: Double): MealNutrition {
        val multiplier = grams / 100.0
        return MealNutrition(
            calories = product.caloriesPer100g * multiplier,
            protein = product.proteinPer100g * multiplier,
            fat = product.fatPer100g * multiplier,
            carbs = product.carbsPer100g * multiplier,
        )
    }

    fun forIngredient(ingredient: DishIngredient): MealNutrition {
        val multiplier = ingredient.grams / 100.0
        return MealNutrition(
            calories = ingredient.productSnapshot.caloriesPer100g * multiplier,
            protein = ingredient.productSnapshot.proteinPer100g * multiplier,
            fat = ingredient.productSnapshot.fatPer100g * multiplier,
            carbs = ingredient.productSnapshot.carbsPer100g * multiplier,
        )
    }

    fun dishNutrition(ingredients: List<DishIngredient>): MealNutrition = MealNutrition(
        calories = ingredients.sumOf { forIngredient(it).calories },
        protein = ingredients.sumOf { forIngredient(it).protein },
        fat = ingredients.sumOf { forIngredient(it).fat },
        carbs = ingredients.sumOf { forIngredient(it).carbs },
    )

    fun forDishPortion(dish: Dish, grams: Double): MealNutrition {
        val ratio = if (dish.totalWeight <= 0.0) 0.0 else grams / dish.totalWeight
        return MealNutrition(
            calories = dish.calories * ratio,
            protein = dish.protein * ratio,
            fat = dish.fat * ratio,
            carbs = dish.carbs * ratio,
        )
    }

    fun forEntry(entry: MealEntry): MealEntryDetails = MealEntryDetails(
        entry = entry,
        name = entry.nameSnapshot,
        calories = entry.caloriesSnapshot,
        protein = entry.proteinSnapshot,
        fat = entry.fatSnapshot,
        carbs = entry.carbsSnapshot,
    )

    fun summarize(date: String, entries: List<MealEntryDetails>): DailySummary = DailySummary(
        date = date,
        calories = entries.sumOf { it.calories },
        protein = entries.sumOf { it.protein },
        fat = entries.sumOf { it.fat },
        carbs = entries.sumOf { it.carbs },
    )
}

data class MealNutrition(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
)
