package com.maks.caloriecounter.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.maks.caloriecounter.data.local.entity.DishEntity
import com.maks.caloriecounter.data.local.entity.DishIngredientEntity

data class DishWithIngredients(
    @Embedded val dish: DishEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "dishId",
    )
    val ingredients: List<DishIngredientEntity>,
)
