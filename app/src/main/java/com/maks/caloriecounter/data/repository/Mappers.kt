package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.entity.DishEntity
import com.maks.caloriecounter.data.local.entity.DishIngredientEntity
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity
import com.maks.caloriecounter.data.local.relation.DishWithIngredients
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.model.FoodLogEntryType
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.model.ProductSnapshot
import com.maks.caloriecounter.domain.util.NutritionCalculator

fun ProductEntity.toDomain(): Product = Product(id, name, caloriesPer100g, proteinPer100g, fatPer100g, carbsPer100g, createdAt, isFavorite, lastUsedAt, barcode, barcodeFormat, source)
fun Product.toEntity(): ProductEntity = ProductEntity(id, name.trim(), caloriesPer100g, proteinPer100g, fatPer100g, carbsPer100g, createdAt, isFavorite, lastUsedAt, barcode, barcodeFormat, source)
fun DishIngredientEntity.toDomain(): DishIngredient = DishIngredient(
    id = id,
    productId = productId,
    productSnapshot = ProductSnapshot(productNameSnapshot, caloriesPer100gSnapshot, proteinPer100gSnapshot, fatPer100gSnapshot, carbsPer100gSnapshot),
    grams = grams,
)

fun DishIngredient.toEntity(dishId: Long): DishIngredientEntity = DishIngredientEntity(
    id = id,
    dishId = dishId,
    productId = productId,
    productNameSnapshot = productSnapshot.name,
    caloriesPer100gSnapshot = productSnapshot.caloriesPer100g,
    proteinPer100gSnapshot = productSnapshot.proteinPer100g,
    fatPer100gSnapshot = productSnapshot.fatPer100g,
    carbsPer100gSnapshot = productSnapshot.carbsPer100g,
    grams = grams,
)

fun DishWithIngredients.toDomain(): Dish = Dish(
    id = dish.id,
    name = dish.name,
    ingredients = ingredients.map { it.toDomain() },
    totalWeight = dish.totalWeight,
    calories = dish.calories,
    protein = dish.protein,
    fat = dish.fat,
    carbs = dish.carbs,
    isFavorite = dish.isFavorite,
    lastUsedAt = dish.lastUsedAt,
    createdAt = dish.createdAt,
    updatedAt = dish.updatedAt,
)

fun Dish.toEntity(): DishEntity = DishEntity(
    id = id,
    name = name.trim(),
    totalWeight = totalWeight,
    calories = calories,
    protein = protein,
    fat = fat,
    carbs = carbs,
    isFavorite = isFavorite,
    lastUsedAt = lastUsedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MealEntryEntity.toDomain(): MealEntry = MealEntry(
    id = id,
    type = FoodLogEntryType.fromStorage(type),
    sourceId = sourceId,
    productId = productId,
    date = date,
    grams = grams,
    amountUnit = amountUnit,
    mealType = MealType.fromStorage(mealType),
    nameSnapshot = nameSnapshot,
    caloriesSnapshot = caloriesSnapshot,
    proteinSnapshot = proteinSnapshot,
    fatSnapshot = fatSnapshot,
    carbsSnapshot = carbsSnapshot,
    createdAt = createdAt,
)

fun MealEntry.toEntity(): MealEntryEntity = MealEntryEntity(
    id = id,
    type = type.storageValue,
    sourceId = sourceId,
    productId = productId,
    date = date,
    grams = grams,
    amountUnit = amountUnit,
    mealType = mealType.name,
    nameSnapshot = nameSnapshot,
    caloriesSnapshot = caloriesSnapshot,
    proteinSnapshot = proteinSnapshot,
    fatSnapshot = fatSnapshot,
    carbsSnapshot = carbsSnapshot,
    createdAt = createdAt,
)

fun MealEntryEntity.toDetails(): MealEntryDetails = NutritionCalculator.forEntry(toDomain())
