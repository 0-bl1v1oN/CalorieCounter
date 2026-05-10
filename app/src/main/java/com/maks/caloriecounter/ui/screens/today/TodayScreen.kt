package com.maks.caloriecounter.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.MealEntryCard
import com.maks.caloriecounter.ui.components.NutritionSummaryCard

@Composable
fun TodayScreen(viewModel: TodayViewModel, onPreviousDay: () -> Unit, onNextDay: () -> Unit, onAddMeal: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onPreviousDay) { Text("← День") }
                Column { Text("Сегодня", style = MaterialTheme.typography.titleLarge); Text(DateUtils.display(state.date)) }
                TextButton(onClick = onNextDay) { Text("День →") }
            }
        }
        item { NutritionSummaryCard(state.summary, state.settings) }
        item { Button(onClick = onAddMeal, modifier = Modifier.fillMaxWidth()) { Text("Добавить еду") } }
        if (state.entries.isEmpty()) {
            item { EmptyState("За этот день пока нет записей") }
        } else {
            items(state.entries, key = { it.entry.id }) { entry ->
                MealEntryCard(entry = entry, onDelete = { viewModel.deleteEntry(entry.entry.id) }, onUpdateGrams = { viewModel.updateGrams(entry.entry.id, it) })
            }
        }
    }
}
