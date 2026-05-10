package com.maks.caloriecounter.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maks.caloriecounter.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {
    private object Keys {
        val CalorieGoal = intPreferencesKey("calorie_goal")
        val ProteinGoal = intPreferencesKey("protein_goal")
        val FatGoal = intPreferencesKey("fat_goal")
        val CarbsGoal = intPreferencesKey("carbs_goal")
    }

    val settings: Flow<UserSettings> = context.userPreferencesDataStore.data.map { preferences ->
        UserSettings(
            calorieGoal = preferences[Keys.CalorieGoal] ?: 2200,
            proteinGoal = preferences[Keys.ProteinGoal] ?: 150,
            fatGoal = preferences[Keys.FatGoal] ?: 70,
            carbsGoal = preferences[Keys.CarbsGoal] ?: 250,
        )
    }

    suspend fun save(settings: UserSettings) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.CalorieGoal] = settings.calorieGoal
            preferences[Keys.ProteinGoal] = settings.proteinGoal
            preferences[Keys.FatGoal] = settings.fatGoal
            preferences[Keys.CarbsGoal] = settings.carbsGoal
        }
    }
}
