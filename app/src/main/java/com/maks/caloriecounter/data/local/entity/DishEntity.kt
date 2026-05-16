package com.maks.caloriecounter.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dishes",
    indices = [Index(value = ["name"], unique = true)],
)
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalWeight: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)
