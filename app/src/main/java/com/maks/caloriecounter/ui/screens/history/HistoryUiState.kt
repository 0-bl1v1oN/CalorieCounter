package com.maks.caloriecounter.ui.screens.history

import com.maks.caloriecounter.domain.model.DailySummary

data class HistoryUiState(val summaries: List<DailySummary> = emptyList())
