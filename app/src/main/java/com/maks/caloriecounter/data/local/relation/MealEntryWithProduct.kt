package com.maks.caloriecounter.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity

data class MealEntryWithProduct(
    @Embedded val entry: MealEntryEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id",
    )
    val product: ProductEntity,
)
