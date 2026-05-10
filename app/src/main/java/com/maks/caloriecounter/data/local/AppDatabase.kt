package com.maks.caloriecounter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maks.caloriecounter.data.local.dao.MealEntryDao
import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, MealEntryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun mealEntryDao(): MealEntryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE products ADD COLUMN lastUsedAt INTEGER")
            }
        }
    }
}
