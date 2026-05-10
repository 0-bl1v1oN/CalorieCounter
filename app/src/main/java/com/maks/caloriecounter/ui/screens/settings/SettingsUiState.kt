package com.maks.caloriecounter.ui.screens.settings

data class SettingsUiState(
    val calorieGoal: String = "2200",
    val proteinGoal: String = "150",
    val fatGoal: String = "70",
    val carbsGoal: String = "250",
    val error: String? = null,
    val savedMessage: String? = null,
)
