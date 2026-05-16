package com.maks.caloriecounter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.maks.caloriecounter.data.local.entity.MealEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY createdAt DESC")
    fun observeEntriesForDate(date: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries ORDER BY date DESC, createdAt DESC")
    fun observeAllEntries(): Flow<List<MealEntryEntity>>

    @Insert
    suspend fun insertEntry(entry: MealEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: MealEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: MealEntryEntity)

    @Query("SELECT * FROM meal_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): MealEntryEntity?
}
