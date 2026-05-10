package com.maks.caloriecounter.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.ui.components.AppTextField
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.ProductCard

@Composable
fun ProductsScreen(viewModel: ProductsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Продукты", style = MaterialTheme.typography.headlineSmall) }
        item { AppTextField(state.query, viewModel::updateQuery, "Поиск") }
        item {
            ProductForm(state, viewModel)
        }
        if (state.products.isEmpty()) item { EmptyState("Сохранённых продуктов пока нет") } else items(state.products, key = { it.id }) { product ->
            ProductCard(product, onEdit = { viewModel.edit(product) }, onDelete = { viewModel.delete(product) }, onQuickAdd = { viewModel.openQuickAdd(product) })
        }
    }
    val quickProduct = state.quickAdd.product
    if (quickProduct != null) {
        AlertDialog(
            onDismissRequest = viewModel::closeQuickAdd,
            title = { Text("Добавить: ${quickProduct.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(state.quickAdd.grams, viewModel::updateQuickGrams, "Граммы", number = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { MealType.entries.forEach { type -> FilterChip(selected = state.quickAdd.mealType == type, onClick = { viewModel.updateQuickMealType(type) }, label = { Text(type.title) }) } }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { TextButton(onClick = viewModel::quickAdd) { Text("Добавить") } },
            dismissButton = { TextButton(onClick = viewModel::closeQuickAdd) { Text("Отмена") } },
        )
    }
}

@Composable
private fun ProductForm(state: ProductsUiState, viewModel: ProductsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (state.editingProductId == null) "Новый продукт" else "Редактирование продукта")
        AppTextField(state.form.name, viewModel::updateFormName, "Название")
        AppTextField(state.form.calories, viewModel::updateFormCalories, "Калории на 100 г", number = true)
        AppTextField(state.form.protein, viewModel::updateFormProtein, "Белки на 100 г", number = true)
        AppTextField(state.form.fat, viewModel::updateFormFat, "Жиры на 100 г", number = true)
        AppTextField(state.form.carbs, viewModel::updateFormCarbs, "Углеводы на 100 г", number = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveProduct, modifier = Modifier.weight(1f)) { Text(if (state.editingProductId == null) "Добавить" else "Сохранить") }
            if (state.editingProductId != null) TextButton(onClick = viewModel::cancelEdit) { Text("Отмена") }
        }
    }
}
