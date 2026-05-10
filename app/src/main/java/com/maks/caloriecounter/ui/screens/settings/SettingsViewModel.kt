package com.maks.caloriecounter.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.SettingsRepository
import com.maks.caloriecounter.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.value = SettingsUiState(
                    calorieGoal = settings.calorieGoal.toString(),
                    proteinGoal = settings.proteinGoal.toString(),
                    fatGoal = settings.fatGoal.toString(),
                    carbsGoal = settings.carbsGoal.toString(),
                )
            }
        }
    }

    fun updateCalories(value: String) = _uiState.update { it.copy(calorieGoal = value, error = null, savedMessage = null) }
    fun updateProtein(value: String) = _uiState.update { it.copy(proteinGoal = value, error = null, savedMessage = null) }
    fun updateFat(value: String) = _uiState.update { it.copy(fatGoal = value, error = null, savedMessage = null) }
    fun updateCarbs(value: String) = _uiState.update { it.copy(carbsGoal = value, error = null, savedMessage = null) }

    fun save() {
        val state = uiState.value
        val calories = state.calorieGoal.toIntOrNull()
        val protein = state.proteinGoal.toIntOrNull()
        val fat = state.fatGoal.toIntOrNull()
        val carbs = state.carbsGoal.toIntOrNull()
        if (listOf(calories, protein, fat, carbs).any { it == null || it < 0 }) {
            _uiState.update { it.copy(error = "Цели должны быть целыми неотрицательными числами") }
            return
        }
        viewModelScope.launch {
            settingsRepository.save(UserSettings(calories!!, protein!!, fat!!, carbs!!))
            _uiState.update { it.copy(savedMessage = "Настройки сохранены") }
        }
    }
}
