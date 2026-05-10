package com.maks.caloriecounter.data.importer

import android.content.Context
import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.data.local.entity.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductCsvImporter(
    private val context: Context,
    private val productDao: ProductDao,
) {
    suspend fun importIfDatabaseIsEmpty() = withContext(Dispatchers.IO) {
        if (productDao.countProducts() > 0) return@withContext

        val products = context.assets.open(PRODUCTS_FILE_NAME).bufferedReader().useLines { lines ->
            lines.mapNotNull(::parseProductLine).toList()
        }

        if (products.isNotEmpty()) {
            productDao.insertProducts(products)
        }
    }

    private fun parseProductLine(line: String): ProductEntity? {
        val columns = line.parseCsvColumns().map { it.trim() }
        if (columns.size < MIN_COLUMNS) return null

        val nutritionStartIndex = columns.indexOfFirstNutritionBlock()
        if (nutritionStartIndex <= 0) return null

        val calories = columns[nutritionStartIndex].toNutritionDoubleOrNull() ?: return null
        val protein = columns[nutritionStartIndex + 1].toNutritionDoubleOrNull() ?: return null
        val fat = columns[nutritionStartIndex + 2].toNutritionDoubleOrNull() ?: return null
        val carbs = columns[nutritionStartIndex + 3].toNutritionDoubleOrNull() ?: return null
        val name = columns.take(nutritionStartIndex).joinToString(", ").trim()

        if (name.isBlank()) return null

        return ProductEntity(
            name = name,
            caloriesPer100g = calories,
            proteinPer100g = protein,
            fatPer100g = fat,
            carbsPer100g = carbs,
        )
    }

    private fun List<String>.indexOfFirstNutritionBlock(): Int {
        val maxStartIndex = size - NUTRITION_COLUMN_COUNT
        for (index in 1..maxStartIndex) {
            val hasNutritionBlock = subList(index, index + NUTRITION_COLUMN_COUNT).all { column ->
                column.toNutritionDoubleOrNull() != null
            }
            if (hasNutritionBlock) return index
        }
        return -1
    }

    private fun String.toNutritionDoubleOrNull(): Double? = replace(',', '.').toDoubleOrNull()

    private fun String.parseCsvColumns(): List<String> {
        val separator = if (';' in this) ';' else ','
        val columns = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < length) {
            val char = this[index]
            when {
                char == '"' && inQuotes && getOrNull(index + 1) == '"' -> {
                    current.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == separator && !inQuotes -> {
                    columns.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }

        columns.add(current.toString())
        return columns
    }

    private companion object {
        const val PRODUCTS_FILE_NAME = "products.csv"
        const val MIN_COLUMNS = 5
        const val NUTRITION_COLUMN_COUNT = 4
    }
}
