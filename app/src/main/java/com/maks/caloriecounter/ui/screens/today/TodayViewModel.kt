package com.maks.caloriecounter.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.SettingsRepository
import com.maks.caloriecounter.domain.util.NutritionCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(
    private val date: String,
    private val mealRepository: MealRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = combine(
        mealRepository.observeEntriesForDate(date),
        settingsRepository.settings,
    ) { entries, settings ->
        TodayUiState(date = date, settings = settings, entries = entries, summary = NutritionCalculator.summarize(date, entries))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState(date))

    fun deleteEntry(entryId: Long) {
        val entry = uiState.value.entries.firstOrNull { it.entry.id == entryId }?.entry ?: return
        viewModelScope.launch { mealRepository.deleteEntry(entry) }
    }

    fun updateGrams(entryId: Long, gramsText: String) {
        val grams = gramsText.replace(',', '.').toDoubleOrNull() ?: return
        if (grams > 0) viewModelScope.launch { mealRepository.updateEntryGrams(entryId, grams) }
    }
}
