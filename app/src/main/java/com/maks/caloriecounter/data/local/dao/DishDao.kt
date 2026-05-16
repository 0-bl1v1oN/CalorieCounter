package com.maks.caloriecounter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.maks.caloriecounter.data.local.entity.DishEntity
import com.maks.caloriecounter.data.local.entity.DishIngredientEntity
import com.maks.caloriecounter.data.local.relation.DishWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Transaction
    @Query("SELECT * FROM dishes ORDER BY name COLLATE NOCASE ASC")
    fun observeDishes(): Flow<List<DishWithIngredients>>

    @Transaction
    @Query("SELECT * FROM dishes WHERE id = :id LIMIT 1")
    suspend fun getDishById(id: Long): DishWithIngredients?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDish(dish: DishEntity): Long

    @Update
    suspend fun updateDish(dish: DishEntity)

    @Insert
    suspend fun insertIngredients(ingredients: List<DishIngredientEntity>)

    @Query("DELETE FROM dish_ingredients WHERE dishId = :dishId")
    suspend fun deleteIngredientsForDish(dishId: Long)

    @Query("DELETE FROM dishes WHERE id = :dishId")
    suspend fun deleteDishById(dishId: Long)

    @Query("UPDATE dishes SET isFavorite = :isFavorite WHERE id = :dishId")
    suspend fun updateFavorite(dishId: Long, isFavorite: Boolean)

    @Query("UPDATE dishes SET lastUsedAt = :lastUsedAt WHERE id = :dishId")
    suspend fun updateLastUsedAt(dishId: Long, lastUsedAt: Long)

    @Transaction
    suspend fun insertDishWithIngredients(dish: DishEntity, ingredients: List<DishIngredientEntity>): Long {
        val dishId = insertDish(dish)
        insertIngredients(ingredients.map { it.copy(dishId = dishId) })
        return dishId
    }

    @Transaction
    suspend fun updateDishWithIngredients(dish: DishEntity, ingredients: List<DishIngredientEntity>) {
        updateDish(dish)
        deleteIngredientsForDish(dish.id)
        insertIngredients(ingredients.map { it.copy(dishId = dish.id) })
    }
}
