package com.maks.caloriecounter.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_entries",
    indices = [Index("sourceId"), Index("productId"), Index("date"), Index("type")],
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "product",
    val sourceId: Long,
    val productId: Long? = null,
    val date: String,
    val grams: Double,
    val amountUnit: String = "g",
    val mealType: String,
    val nameSnapshot: String,
    val caloriesSnapshot: Double,
    val proteinSnapshot: Double,
    val fatSnapshot: Double,
    val carbsSnapshot: Double,
    val createdAt: Long = System.currentTimeMillis(),
)
