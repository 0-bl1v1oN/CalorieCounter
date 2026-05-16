package com.maks.caloriecounter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maks.caloriecounter.data.local.dao.DishDao
import com.maks.caloriecounter.data.local.dao.MealEntryDao
import com.maks.caloriecounter.data.local.dao.ProductDao
import com.maks.caloriecounter.data.local.entity.DishEntity
import com.maks.caloriecounter.data.local.entity.DishIngredientEntity
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import com.maks.caloriecounter.data.local.entity.ProductEntity

@Database(
    entities = [ProductEntity::class, MealEntryEntity::class, DishEntity::class, DishIngredientEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun dishDao(): DishDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE products ADD COLUMN lastUsedAt INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT")
                db.execSQL("ALTER TABLE products ADD COLUMN barcodeFormat TEXT")
                db.execSQL("ALTER TABLE products ADD COLUMN source TEXT NOT NULL DEFAULT 'manual'")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dishes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        totalWeight REAL NOT NULL,
                        calories REAL NOT NULL,
                        protein REAL NOT NULL,
                        fat REAL NOT NULL,
                        carbs REAL NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        lastUsedAt INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_dishes_name ON dishes(name)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dish_ingredients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dishId INTEGER NOT NULL,
                        productId INTEGER NOT NULL,
                        productNameSnapshot TEXT NOT NULL,
                        caloriesPer100gSnapshot REAL NOT NULL,
                        proteinPer100gSnapshot REAL NOT NULL,
                        fatPer100gSnapshot REAL NOT NULL,
                        carbsPer100gSnapshot REAL NOT NULL,
                        grams REAL NOT NULL,
                        FOREIGN KEY(dishId) REFERENCES dishes(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_dish_ingredients_dishId ON dish_ingredients(dishId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_dish_ingredients_productId ON dish_ingredients(productId)")

                db.execSQL(
                    """
                    CREATE TABLE meal_entries_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL DEFAULT 'product',
                        sourceId INTEGER NOT NULL,
                        productId INTEGER,
                        date TEXT NOT NULL,
                        grams REAL NOT NULL,
                        amountUnit TEXT NOT NULL DEFAULT 'g',
                        mealType TEXT NOT NULL,
                        nameSnapshot TEXT NOT NULL,
                        caloriesSnapshot REAL NOT NULL,
                        proteinSnapshot REAL NOT NULL,
                        fatSnapshot REAL NOT NULL,
                        carbsSnapshot REAL NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO meal_entries_new (
                        id, type, sourceId, productId, date, grams, amountUnit, mealType,
                        nameSnapshot, caloriesSnapshot, proteinSnapshot, fatSnapshot, carbsSnapshot, createdAt
                    )
                    SELECT
                        meal_entries.id,
                        'product',
                        meal_entries.productId,
                        meal_entries.productId,
                        meal_entries.date,
                        meal_entries.grams,
                        'g',
                        meal_entries.mealType,
                        products.name,
                        products.caloriesPer100g * meal_entries.grams / 100.0,
                        products.proteinPer100g * meal_entries.grams / 100.0,
                        products.fatPer100g * meal_entries.grams / 100.0,
                        products.carbsPer100g * meal_entries.grams / 100.0,
                        meal_entries.createdAt
                    FROM meal_entries
                    JOIN products ON products.id = meal_entries.productId
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE meal_entries")
                db.execSQL("ALTER TABLE meal_entries_new RENAME TO meal_entries")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_entries_sourceId ON meal_entries(sourceId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_entries_productId ON meal_entries(productId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_entries_date ON meal_entries(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_entries_type ON meal_entries(type)")
            }
        }
    }
}
