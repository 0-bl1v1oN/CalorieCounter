package com.maks.caloriecounter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maks.caloriecounter.data.local.dao.MealEntryDao
import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, MealEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun mealEntryDao(): MealEntryDao
}
