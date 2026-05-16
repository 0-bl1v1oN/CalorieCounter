package com.maks.caloriecounter.ui.screens.today

import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.UserSettings

data class WeeklyCaloriesPoint(
    val date: String,
    val calories: Double,
)

data class TodayUiState(
    val date: String,
    val settings: UserSettings = UserSettings(),
    val entries: List<MealEntryDetails> = emptyList(),
    val summary: DailySummary = DailySummary(date, 0.0, 0.0, 0.0, 0.0),
    val weekCalories: List<WeeklyCaloriesPoint> = emptyList(),
)
