package com.maks.caloriecounter.ui.screens.addmeal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.ui.components.AppTextField

@Composable
fun AddMealScreen(viewModel: AddMealViewModel, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Добавить еду", style = MaterialTheme.typography.headlineSmall)
        AppTextField(state.name, viewModel::updateName, "Название продукта")
        AppTextField(state.grams, viewModel::updateGrams, "Граммы", number = true)
        AppTextField(state.calories, viewModel::updateCalories, "Калории на 100 г", number = true)
        AppTextField(state.protein, viewModel::updateProtein, "Белки на 100 г", number = true)
        AppTextField(state.fat, viewModel::updateFat, "Жиры на 100 г", number = true)
        AppTextField(state.carbs, viewModel::updateCarbs, "Углеводы на 100 г", number = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { type -> FilterChip(selected = state.mealType == type, onClick = { viewModel.updateMealType(type) }, label = { Text(type.title) }) }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
    }
}
