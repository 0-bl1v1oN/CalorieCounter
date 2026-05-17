package com.maks.caloriecounter.ui.screens.dishes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.DishIngredient
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.util.NutritionCalculator
import com.maks.caloriecounter.ui.components.AppTextField
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishFormScreen(
    viewModel: DishFormViewModel,
    onBack: () -> Unit,
    onPickProducts: () -> Unit,
    onCreateProduct: () -> Unit,
    onSaved: () -> Unit,
    selectedProductIds: String?,
    onSelectedProductIdsConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(selectedProductIds) {
        val ids = selectedProductIds.orEmpty().split(',').mapNotNull { it.toLongOrNull() }
        if (ids.isNotEmpty()) {
            viewModel.addProductsByIds(ids)
            onSelectedProductIdsConsumed()
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.clearSaved()
            onSaved()
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = "Новое блюдо", onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { AppTextField(state.name, viewModel::updateName, "Название блюда", placeholder = "Например: Творог со сметаной") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPickProducts,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.58f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text("Ингредиенты", modifier = Modifier.padding(start = 6.dp))
                    }
                    OutlinedButton(
                        onClick = onCreateProduct,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.42f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text("Продукт", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            item {
                Text(
                    text = "Состав блюда",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (state.ingredients.isEmpty()) {
                item { DishIngredientsEmptyState() }
            } else {
                items(state.ingredients, key = { "${it.id}-${it.productId}" }) { ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onGramsChange = { viewModel.updateIngredientGrams(ingredient.id, ingredient.productId, it) },
                        onRemove = { viewModel.removeIngredient(ingredient) },
                    )
                }
            }
            item { DishTotalsCard(totalWeight = state.totalWeight, calories = state.nutrition.calories, protein = state.nutrition.protein, fat = state.nutrition.fat, carbs = state.nutrition.carbs) }
            state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CtaColor,
                        contentColor = Color.White,
                    ),
                ) { Text("Сохранить блюдо") }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: DishIngredient, onGramsChange: (String) -> Unit, onRemove: () -> Unit) {
    val product = ingredient.productSnapshot
    PremiumCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${product.caloriesPer100g.kcal()} ккал / 100 г",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                CompactMacroLine(
                    protein = product.proteinPer100g,
                    fat = product.fatPer100g,
                    carbs = product.carbsPer100g,
                )
            }
            AppTextField(
                value = ingredient.grams.grams(),
                onValueChange = onGramsChange,
                label = "г",
                modifier = Modifier.width(84.dp),
                number = true,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@Composable
private fun DishIngredientsEmptyState() {
    PremiumCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Text(
            text = "Добавьте продукты в состав блюда — КБЖУ посчитаются автоматически.",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DishTotalsCard(totalWeight: Double, calories: Double, protein: Double, fat: Double, carbs: Double) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Итого по блюду", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${totalWeight.grams()} г · ${calories.kcal()} ккал", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            MacroLine(protein, fat, carbs)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerScreen(viewModel: ProductPickerViewModel, onBack: () -> Unit, onAdd: (List<Long>) -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar("Выбор продуктов", onBack) },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Выбрано: ${state.selectedIds.size}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { onAdd(state.selectedIds.toList()) },
                    enabled = state.selectedIds.isNotEmpty(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CtaColor,
                        contentColor = Color.White,
                    ),
                ) { Text("Добавить") }
            }
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SearchField(state.searchQuery, viewModel::updateSearchQuery) }
            if (state.products.isEmpty()) item { EmptyState("Продукты не найдены") } else items(state.products, key = { it.id }) { product ->
                PremiumCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Checkbox(checked = product.id in state.selectedIds, onCheckedChange = { viewModel.toggleProduct(product.id) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(product.caloriesPer100g.kcal() + " ккал на 100 г", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishLogScreen(viewModel: DishLogViewModel, onBack: () -> Unit, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val dish = state.dish
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    Scaffold(modifier = modifier.fillMaxSize(), topBar = { AppTopBar("Добавить блюдо", onBack) }) { innerPadding ->
        if (dish == null) {
            EmptyState("Блюдо не найдено")
        } else {
            val grams = state.grams.replace(',', '.').toDoubleOrNull() ?: 0.0
            val n = NutritionCalculator.forDishPortion(dish, grams)
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { DishInfoCard(dish = dish, onToggleFavorite = viewModel::toggleFavorite) }
                item { AppTextField(state.grams, viewModel::updateGrams, "Граммы", number = true) }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Приём пищи", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MealType.todaySections) { type ->
                                FilterChip(selected = state.mealType == type, onClick = { viewModel.updateMealType(type) }, label = { Text(type.title) }, shape = RoundedCornerShape(16.dp))
                            }
                        }
                    }
                }
                item { DishTotalsCard(totalWeight = grams, calories = n.calories, protein = n.protein, fat = n.fat, carbs = n.carbs) }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                item {
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CtaColor,
                            contentColor = Color.White,
                        ),
                    ) { Text("Добавить") }
                }
            }
        }
    }
}

@Composable
private fun DishInfoCard(dish: Dish, onToggleFavorite: () -> Unit) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(CtaColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(22.dp), tint = CtaColor)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(dish.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Блюдо", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
                Text("${dish.totalWeight.grams()} г · ${dish.calories.kcal()} ккал", color = MaterialTheme.colorScheme.onSurfaceVariant)
                MacroLine(dish.protein, dish.fat, dish.carbs)
            }
            FavoriteIconButton(isFavorite = dish.isFavorite, onClick = onToggleFavorite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Назад") } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        placeholder = { Text("Поиск продукта") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
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
private fun MacroLine(protein: Double, fat: Double, carbs: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Б ${protein.grams()}", color = ProteinColor, style = MaterialTheme.typography.bodyMedium)
        Text("Ж ${fat.grams()}", color = FatColor, style = MaterialTheme.typography.bodyMedium)
        Text("У ${carbs.grams()}", color = CarbsColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FavoriteIconButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isFavorite) FavoriteColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f),
                shape = CircleShape,
            ),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = if (isFavorite) FavoriteColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
        ),
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun CompactMacroLine(protein: Double, fat: Double, carbs: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Б ${protein.grams()}", color = ProteinColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        Text("Ж ${fat.grams()}", color = FatColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        Text("У ${carbs.grams()}", color = CarbsColor, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.98f),
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                    ),
                ),
            ),
        ) {
            content()
        }
    }
}

private val CtaColor = Color(0xFFC83A7A)
private val FavoriteColor = Color(0xFFD7B56D)

private val ProteinColor = Color(0xFF9FB2FF)
private val FatColor = Color(0xFFFFB020)
private val CarbsColor = Color(0xFFFF5C9A)
