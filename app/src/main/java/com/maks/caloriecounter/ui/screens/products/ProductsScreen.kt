package com.maks.caloriecounter.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.Product
import com.maks.caloriecounter.ui.components.AppTextField
import com.maks.caloriecounter.ui.components.EmptyState
import com.maks.caloriecounter.ui.components.ProductCard
import com.maks.caloriecounter.ui.components.kcal
import com.maks.caloriecounter.ui.components.nutritionLine

@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
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
            FloatingActionButton(onClick = onAddProduct) {
                Text("+", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ProductsHeader(state, viewModel) }

            if (state.selectedFilter == ProductFilter.All && state.favoriteProducts.isNotEmpty()) {
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

            if (state.filteredProducts.isEmpty() && !state.isLoading) {
                item { EmptyState("Продукты не найдены") }
            } else {
                items(state.filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onOpenActions = { viewModel.openActions(product) },
                        onToggleFavorite = { viewModel.toggleFavorite(product) },
                        onQuickAdd = { viewModel.openQuickAdd(product) },
                    )
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

    DeleteConfirmationDialog(
        product = state.deleteConfirmationProduct,
        onDismiss = viewModel::dismissDeleteConfirmation,
        onConfirm = { product -> viewModel.deleteProduct(product) },
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
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProductFilter.entries) { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter.title) },
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
        modifier = Modifier.width(156.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
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
                "${product.caloriesPer100g.kcal()} ккал",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                    MealType.entries.forEach { type ->
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

@Composable
fun ProductFormScreen(
    viewModel: ProductsViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    productId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        if (productId == null) viewModel.startAddProduct() else viewModel.loadProductForEdit(productId)
    }

    LaunchedEffect(state.isFormSaved) {
        if (state.isFormSaved) {
            viewModel.clearFormSaved()
            onSaved()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (productId == null) "Добавить продукт" else "Редактировать продукт",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Пищевая ценность указывается на 100 грамм продукта.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { AppTextField(state.form.name, viewModel::updateFormName, "Название", Modifier.fillMaxWidth(), placeholder = "Например: Гречка варёная") }
        item { AppTextField(state.form.calories, viewModel::updateFormCalories, "Калории на 100 г", Modifier.fillMaxWidth(), number = true, placeholder = "Например: 110") }
        item { AppTextField(state.form.protein, viewModel::updateFormProtein, "Белки на 100 г", Modifier.fillMaxWidth(), number = true, placeholder = "Например: 3.6") }
        item { AppTextField(state.form.fat, viewModel::updateFormFat, "Жиры на 100 г", Modifier.fillMaxWidth(), number = true, placeholder = "Например: 1.1") }
        item { AppTextField(state.form.carbs, viewModel::updateFormCarbs, "Углеводы на 100 г", Modifier.fillMaxWidth(), number = true, placeholder = "Например: 20.5") }
        state.errorMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::saveProduct, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Отмена") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
