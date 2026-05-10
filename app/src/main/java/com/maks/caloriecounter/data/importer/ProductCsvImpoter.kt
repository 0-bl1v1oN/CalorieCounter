package com.maks.caloriecounter.data.importer

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.data.local.entity.ProductEntity
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.productImportDataStore by preferencesDataStore(name = "product_import_metadata")

class ProductCsvImporter(
    private val context: Context,
    private val productDao: ProductDao,
) {
    suspend fun importIfNeeded() = withContext(Dispatchers.IO) {
        val csvBytes = context.assets.open(PRODUCTS_FILE_NAME).use { input -> input.readBytes() }
        val csvHash = csvBytes.sha256()
        val lastImportedHash = context.productImportDataStore.data.first()[PRODUCTS_CSV_HASH_KEY]
        val hasProducts = productDao.countProducts() > 0

        if (hasProducts && lastImportedHash == csvHash) return@withContext

        val products = csvBytes.toString(Charsets.UTF_8).lineSequence()
            .mapNotNull(::parseProductLine)
            .toList()

        if (products.isEmpty()) return@withContext

        productDao.insertProducts(products)
        context.productImportDataStore.edit { preferences ->
            preferences[PRODUCTS_CSV_HASH_KEY] = csvHash
        }
    }

    private fun parseProductLine(line: String): ProductEntity? {
        if (line.isBlank()) return null

        val columns = line.parseCsvColumns().map { it.trim().trimStart(BOM_CHAR) }
        if (columns.size < MIN_COLUMNS || columns.isHeader()) return null

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

    private fun List<String>.isHeader(): Boolean =
        firstOrNull().equals("name", ignoreCase = true) &&
            getOrNull(1).equals("caloriesPer100g", ignoreCase = true)

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

    private fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(this)
        .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private companion object {
        const val PRODUCTS_FILE_NAME = "products.csv"
        const val MIN_COLUMNS = 5
        const val NUTRITION_COLUMN_COUNT = 4
        const val BOM_CHAR = '\uFEFF'
        val PRODUCTS_CSV_HASH_KEY = stringPreferencesKey("products_csv_hash")
    }
}
