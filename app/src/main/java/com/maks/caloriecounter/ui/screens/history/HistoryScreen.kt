package com.maks.caloriecounter.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onDateClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("История", style = MaterialTheme.typography.headlineSmall) }
        if (state.summaries.isEmpty()) item { EmptyState("История пока пуста") } else items(state.summaries, key = { it.date }) { summary ->
            Card(Modifier.fillMaxWidth().clickable { onDateClick(summary.date) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(DateUtils.display(summary.date), style = MaterialTheme.typography.titleMedium)
                    Text("${summary.calories.kcal()} ккал • Б ${summary.protein.grams()} г • Ж ${summary.fat.grams()} г • У ${summary.carbs.grams()} г")
                }
            }
        }
    }
}
