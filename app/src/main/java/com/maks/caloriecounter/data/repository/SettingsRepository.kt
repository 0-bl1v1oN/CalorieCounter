package com.maks.caloriecounter.data.repository

import com.maks.caloriecounter.data.preferences.UserPreferencesDataStore
import com.maks.caloriecounter.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: UserPreferencesDataStore) {
    val settings: Flow<UserSettings> = dataStore.settings

    suspend fun save(settings: UserSettings) = dataStore.save(settings)
}
