package com.maks.caloriecounter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dish_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dishId"), Index("productId")],
)
data class DishIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val caloriesPer100gSnapshot: Double,
    val proteinPer100gSnapshot: Double,
    val fatPer100gSnapshot: Double,
    val carbsPer100gSnapshot: Double,
    val grams: Double,
)
