package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity
import com.maks.caloriecounter.data.local.relation.MealEntryWithProduct
import com.maks.caloriecounter.domain.model.MealEntry
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.domain.util.NutritionCalculator

fun ProductEntity.toDomain(): Product = Product(id, name, caloriesPer100g, proteinPer100g, fatPer100g, carbsPer100g, createdAt)
fun Product.toEntity(): ProductEntity = ProductEntity(id, name.trim(), caloriesPer100g, proteinPer100g, fatPer100g, carbsPer100g, createdAt)
fun MealEntryEntity.toDomain(): MealEntry = MealEntry(id, productId, date, grams, MealType.fromStorage(mealType), createdAt)
fun MealEntry.toEntity(): MealEntryEntity = MealEntryEntity(id, productId, date, grams, mealType.name, createdAt)
fun MealEntryWithProduct.toDomain(): MealEntryDetails = NutritionCalculator.forEntry(entry.toDomain(), product.toDomain())
