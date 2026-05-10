package com.maks.caloriecounter.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.ui.components.AppTextField

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall)
        AppTextField(state.calorieGoal, viewModel::updateCalories, "Цель калорий", number = true)
        AppTextField(state.proteinGoal, viewModel::updateProtein, "Цель белков, г", number = true)
        AppTextField(state.fatGoal, viewModel::updateFat, "Цель жиров, г", number = true)
        AppTextField(state.carbsGoal, viewModel::updateCarbs, "Цель углеводов, г", number = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.savedMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) { Text("Сохранить настройки") }
    }
}
