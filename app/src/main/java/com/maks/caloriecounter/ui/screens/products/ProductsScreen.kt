package com.maks.caloriecounter.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.Dish
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.ui.components.AppTextField
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.ProductCard
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal
import com.maks.caloriecounter.ui.components.nutritionLine

@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onAddDish: () -> Unit,
    onEditDish: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSnackbarMessage()
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("+ Продукт") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 148.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProductsHeader(state, viewModel) }

            if (state.selectedFilter == ProductFilter.All && state.favoriteProducts.size >= 2) {
                item { FavoritesBlock(state.favoriteProducts, viewModel) }
            }

            item {
                Text(
                    text = when (state.selectedFilter) {
                        ProductFilter.All -> "Все продукты"
                        ProductFilter.Favorites -> "Избранные"
                        ProductFilter.Recent -> "Недавние"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.filteredProducts.isEmpty() && state.filteredDishes.isEmpty() && !state.isLoading) {
                item { EmptyState("Продукты и блюда не найдены") }
            } else {
                if (state.filteredProducts.isNotEmpty()) {
                    item { Text("Продукты", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                    items(state.filteredProducts, key = { "product-${it.id}" }) { product ->
                        ProductCard(
                            product = product,
                            onOpenActions = { viewModel.openActions(product) },
                            onToggleFavorite = { viewModel.toggleFavorite(product) },
                            onQuickAdd = { viewModel.openQuickAdd(product) },
                        )
                    }
                }
                if (state.filteredDishes.isNotEmpty()) {
                    item { Text("Блюда", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
                    items(state.filteredDishes, key = { "dish-${it.id}" }) { dish ->
                        DishManageCard(dish = dish, onOpenActions = { viewModel.openDishActions(dish) })
                    }
                }
                item {
                    OutlinedButton(onClick = onAddDish, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(20.dp)) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text("Создать блюдо", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    ProductActionsDialog(
        product = state.actionsProduct,
        onDismiss = viewModel::closeActions,
        onQuickAdd = { product -> viewModel.openQuickAdd(product) },
        onEdit = { product ->
            viewModel.closeActions()
            onEditProduct(product.id)
        },
        onDelete = { product -> viewModel.requestDeleteProduct(product) },
        onToggleFavorite = { product -> viewModel.toggleFavorite(product) },
    )

    DishActionsDialog(
        dish = state.actionsDish,
        onDismiss = viewModel::closeActions,
        onEdit = { dish ->
            viewModel.closeActions()
            onEditDish(dish.id)
        },
        onDelete = { dish -> viewModel.requestDeleteDish(dish) },
        onToggleFavorite = { dish -> viewModel.toggleDishFavorite(dish) },
    )

    DeleteConfirmationDialog(
        product = state.deleteConfirmationProduct,
        onDismiss = viewModel::dismissDeleteConfirmation,
        onConfirm = { product -> viewModel.deleteProduct(product) },
    )

    DishDeleteConfirmationDialog(
        dish = state.deleteConfirmationDish,
        onDismiss = viewModel::dismissDeleteConfirmation,
        onConfirm = { dish -> viewModel.deleteDish(dish) },
    )

    QuickAddDialog(state, viewModel)
}

@Composable
private fun ProductsHeader(state: ProductsUiState, viewModel: ProductsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Продукты", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Поиск") },
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
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProductFilter.entries) { filter ->
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
}

@Composable
private fun FavoritesBlock(favorites: List<Product>, viewModel: ProductsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Избранные", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(favorites, key = { it.id }) { product ->
                FavoriteProductCard(product = product, onClick = { viewModel.openQuickAdd(product) })
            }
        }
    }
}

@Composable
private fun FavoriteProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${product.caloriesPer100g.kcal()} ккал / 100 г",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DishManageCard(dish: Dish, onOpenActions: () -> Unit) {
    Card(
        onClick = onOpenActions,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(dish.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Блюдо", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            Text("${dish.totalWeight.grams()} г · ${dish.calories.kcal()} ккал", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Б ${dish.protein.grams()} · Ж ${dish.fat.grams()} · У ${dish.carbs.grams()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProductActionsDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onQuickAdd: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onToggleFavorite: (Product) -> Unit,
) {
    if (product == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onQuickAdd(product) }, modifier = Modifier.fillMaxWidth()) { Text("Добавить в дневник") }
                TextButton(onClick = { onEdit(product) }, modifier = Modifier.fillMaxWidth()) { Text("Редактировать") }
                TextButton(onClick = { onToggleFavorite(product) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (product.isFavorite) "Убрать из избранного" else "Добавить в избранное")
                }
                TextButton(onClick = { onDelete(product) }, modifier = Modifier.fillMaxWidth()) { Text("Удалить") }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun DishActionsDialog(
    dish: Dish?,
    onDismiss: () -> Unit,
    onEdit: (Dish) -> Unit,
    onDelete: (Dish) -> Unit,
    onToggleFavorite: (Dish) -> Unit,
) {
    if (dish == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dish.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onEdit(dish) }, modifier = Modifier.fillMaxWidth()) { Text("Редактировать") }
                TextButton(onClick = { onToggleFavorite(dish) }, modifier = Modifier.fillMaxWidth()) { Text(if (dish.isFavorite) "Убрать из избранного" else "Добавить в избранное") }
                TextButton(onClick = { onDelete(dish) }, modifier = Modifier.fillMaxWidth()) { Text("Удалить") }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun DeleteConfirmationDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit,
) {
    if (product == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить продукт?") },
        text = { Text(product.name) },
        confirmButton = { TextButton(onClick = { onConfirm(product) }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun DishDeleteConfirmationDialog(
    dish: Dish?,
    onDismiss: () -> Unit,
    onConfirm: (Dish) -> Unit,
) {
    if (dish == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить блюдо ‘${dish.name}’?") },
        text = { Text("История питания не изменится, потому что записи хранят snapshot КБЖУ.") },
        confirmButton = { TextButton(onClick = { onConfirm(dish) }) { Text("Удалить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}

@Composable
private fun QuickAddDialog(state: ProductsUiState, viewModel: ProductsViewModel) {
    val product = state.quickAdd.product ?: return

    AlertDialog(
        onDismissRequest = viewModel::closeQuickAdd,
        title = { Text("Добавить: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(product.nutritionLine(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                AppTextField(state.quickAdd.grams, viewModel::updateQuickGrams, "Граммы", number = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.todaySections.forEach { type ->
                        FilterChip(
                            selected = state.quickAdd.mealType == type,
                            onClick = { viewModel.updateQuickMealType(type) },
                            label = { Text(type.title) },
                        )
                    }
                }
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::quickAddProduct) { Text("Добавить") } },
        dismissButton = { TextButton(onClick = viewModel::closeQuickAdd) { Text("Отмена") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: ProductsViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    productId: Long? = null,
    scannedBarcode: String? = null,
    barcodeFormat: String? = null,
    initialForm: ProductFormState? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(productId, scannedBarcode, barcodeFormat, initialForm) {
        if (productId == null) viewModel.startAddProduct(scannedBarcode, barcodeFormat, initialForm) else viewModel.loadProductForEdit(productId)
    }

    LaunchedEffect(state.isFormSaved) {
        if (state.isFormSaved) {
            viewModel.clearFormSaved()
            onSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Добавить продукт" else "Редактировать продукт") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "Пищевая ценность указывается на 100 грамм продукта.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        if (!state.form.barcode.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(
                            text = "Штрихкод будет привязан к продукту",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            item {
                AppTextField(
                    value = state.form.name,
                    onValueChange = viewModel::updateFormName,
                    label = "Название",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Например: Гречка варёная",
                )
            }
            item {
                AppTextField(
                    value = state.form.calories,
                    onValueChange = viewModel::updateFormCalories,
                    label = "Калории на 100 г",
                    modifier = Modifier.fillMaxWidth(),
                    number = true,
                    placeholder = "Например: 110",
                    suffix = "ккал",
                )
            }
            item {
                AppTextField(
                    value = state.form.protein,
                    onValueChange = viewModel::updateFormProtein,
                    label = "Белки на 100 г",
                    modifier = Modifier.fillMaxWidth(),
                    number = true,
                    placeholder = "Например: 3.6",
                    suffix = "г",
                )
            }
            item {
                AppTextField(
                    value = state.form.fat,
                    onValueChange = viewModel::updateFormFat,
                    label = "Жиры на 100 г",
                    modifier = Modifier.fillMaxWidth(),
                    number = true,
                    placeholder = "Например: 1.1",
                    suffix = "г",
                )
            }
            item {
                AppTextField(
                    value = state.form.carbs,
                    onValueChange = viewModel::updateFormCarbs,
                    label = "Углеводы на 100 г",
                    modifier = Modifier.fillMaxWidth(),
                    number = true,
                    placeholder = "Например: 20.5",
                    suffix = "г",
                )
            }
            state.errorMessage?.let { message ->
                item { Text(message, color = MaterialTheme.colorScheme.error) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = viewModel::saveProduct,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) { Text("Сохранить") }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                    ) { Text("Отмена") }
                }
            }
        }
    }
}
