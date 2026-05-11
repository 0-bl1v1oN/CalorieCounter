package com.maks.caloriecounter.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.UserSettings
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.components.MealEntryCard
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val entriesByMeal = state.entries.groupBy { it.entry.mealType }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TodayHeader(
                date = state.date,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
            )
        }
        item { DailyProgressCard(summary = state.summary, settings = state.settings) }
        item {
            Button(
                onClick = onAddMeal,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Добавить еду",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (state.entries.isEmpty()) {
            item {
                Text(
                    text = "Добавьте первый продукт за сегодня",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        MealType.entries.forEach { mealType ->
            item {
                MealSectionTitle(title = mealType.title)
            }
            val mealEntries = entriesByMeal[mealType].orEmpty()
            if (mealEntries.isEmpty()) {
                item(key = "empty-${mealType.name}") { EmptyMealSection() }
            } else {
                items(mealEntries, key = { it.entry.id }) { entry ->
                    MealEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.entry.id) },
                        onUpdateGrams = { viewModel.updateGrams(entry.entry.id, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayHeader(
    date: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (date == DateUtils.today()) "Сегодня" else "Дневник",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = DateUtils.displayDayWithWeekday(date),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Предыдущий день",
            )
        }
        IconButton(onClick = onNextDay) {
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = "Следующий день",
            )
        }
    }
}

@Composable
private fun DailyProgressCard(
    summary: DailySummary,
    settings: UserSettings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Съедено",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${summary.calories.kcal()} / ${settings.calorieGoal} ккал",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Осталось: ${(settings.calorieGoal - summary.calories).coerceAtLeast(0.0).kcal()} ккал",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { progress(summary.calories, settings.calorieGoal.toDouble()) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            MacroProgressRow("Белки", summary.protein, settings.proteinGoal)
            MacroProgressRow("Жиры", summary.fat, settings.fatGoal)
            MacroProgressRow("Углеводы", summary.carbs, settings.carbsGoal)
        }
    }
}

@Composable
private fun MacroProgressRow(
    title: String,
    value: Double,
    goal: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${value.grams()} / $goal г",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LinearProgressIndicator(
            progress = { progress(value, goal.toDouble()) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun MealSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun EmptyMealSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Text(
            text = "Нет записей",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        )
    }
}

private fun progress(value: Double, goal: Double): Float {
    if (goal <= 0.0) return 0f
    return (value / goal).toFloat().coerceIn(0f, 1f)
}