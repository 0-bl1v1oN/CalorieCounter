package com.maks.caloriecounter.ui.screens.addmeal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.ui.components.AppTextField
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal
import com.maks.caloriecounter.ui.components.nutritionLine

@Composable
fun AddMealScreen(
    viewModel: AddMealViewModel,
    onSaved: () -> Unit,
    onCreateProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    if (state.selectedProduct == null) {
        ProductPickerContent(
            state = state,
            viewModel = viewModel,
            onCreateProduct = onCreateProduct,
            modifier = modifier,
        )
    } else {
        QuickAddContent(
            state = state,
            viewModel = viewModel,
            modifier = modifier,
        )
    }
}

@Composable
private fun ProductPickerContent(
    state: AddMealUiState,
    viewModel: AddMealViewModel,
    onCreateProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Добавить еду", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Выберите продукт из базы. КБЖУ подтянутся автоматически.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ProductSearchField(state.searchQuery, viewModel::updateSearchQuery)
                AddMealFilters(state, viewModel)
                OutlinedButton(
                    onClick = onCreateProduct,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Создать новый продукт",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        item {
            Text(
                text = when (state.selectedFilter) {
                    AddMealProductFilter.All -> "Все продукты"
                    AddMealProductFilter.Favorites -> "Избранные"
                    AddMealProductFilter.Recent -> "Недавние"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.filteredProducts.isEmpty() && !state.isLoading) {
            item { EmptyState("Продукты не найдены") }
        } else {
            items(state.filteredProducts, key = { it.id }) { product ->
                AddMealProductCard(
                    product = product,
                    onSelect = { viewModel.selectProduct(product) },
                )
            }
        }
    }
}

@Composable
private fun ProductSearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Поиск продукта") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
private fun AddMealFilters(state: AddMealUiState, viewModel: AddMealViewModel) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AddMealProductFilter.entries) { filter ->
            FilterChip(
                selected = state.selectedFilter == filter,
                onClick = { viewModel.selectFilter(filter) },
                label = { Text(filter.title, style = MaterialTheme.typography.labelLarge) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = state.selectedFilter == filter,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                ),
            )
        }
    }
}

@Composable
private fun AddMealProductCard(product: Product, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = product.nutritionLine(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(
                onClick = onSelect,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) { Text("Выбрать") }
        }
    }
}

@Composable
private fun QuickAddContent(
    state: AddMealUiState,
    viewModel: AddMealViewModel,
    modifier: Modifier = Modifier,
) {
    val product = requireNotNull(state.selectedProduct)
    val grams = state.grams.asDouble() ?: 0.0
    val multiplier = grams / 100.0
    val calories = product.caloriesPer100g * multiplier
    val protein = product.proteinPer100g * multiplier
    val fat = product.fatPer100g * multiplier
    val carbs = product.carbsPer100g * multiplier

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::clearSelectedProduct) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад к выбору продукта")
                }
                Text("Добавить еду", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(product.nutritionLine(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { AppTextField(state.grams, viewModel::updateGrams, "Граммы", number = true) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Приём пищи", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MealType.todaySections) { type ->
                        FilterChip(
                            selected = state.mealType == type,
                            onClick = { viewModel.updateMealType(type) },
                            label = { Text(type.title) },
                            shape = RoundedCornerShape(16.dp),
                        )
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Предварительный расчёт", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${grams.grams()} г · ${calories.kcal()} ккал", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Б ${protein.grams()} · Ж ${fat.grams()} · У ${carbs.grams()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        state.error?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) { Text("Добавить") }
        }
    }
}
