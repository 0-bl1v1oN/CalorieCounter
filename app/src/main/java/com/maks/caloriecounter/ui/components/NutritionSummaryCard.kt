package com.maks.caloriecounter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.UserSettings

@Composable
fun NutritionSummaryCard(summary: DailySummary, settings: UserSettings, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Дневная цель: ${settings.calorieGoal} ккал", style = MaterialTheme.typography.titleMedium)
            Text("Съедено: ${summary.calories.kcal()} ккал")
            Text("Осталось: ${(settings.calorieGoal - summary.calories).coerceAtLeast(0.0).kcal()} ккал")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Б: ${summary.protein.grams()} / ${settings.proteinGoal} г")
                Text("Ж: ${summary.fat.grams()} / ${settings.fatGoal} г")
                Text("У: ${summary.carbs.grams()} / ${settings.carbsGoal} г")
            }
        }
    }
}
