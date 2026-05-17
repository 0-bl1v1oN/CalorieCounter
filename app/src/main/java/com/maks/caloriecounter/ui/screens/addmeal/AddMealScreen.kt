package com.maks.caloriecounter.ui.screens.addmeal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsProduct
import com.maks.caloriecounter.domain.model.Dish
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
    onCreateProduct: (String?, String?, OpenFoodFactsProduct?) -> Unit,
    onCreateDish: () -> Unit,
    onSelectDish: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scanner =
        remember(context) {
            val options =
                GmsBarcodeScannerOptions
                    .Builder()
                    .enableAutoZoom()
                    .build()
            GmsBarcodeScanning.getClient(context, options)
        }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearSnackbarMessage()
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        if (state.selectedProduct == null) {
            ProductPickerContent(
                state = state,
                viewModel = viewModel,
                onCreateProduct = { onCreateProduct(null, null, null) },
                onCreateDish = onCreateDish,
                onSelectDish = onSelectDish,
                onScanBarcode = {
                    scanner
                        .startScan()
                        .addOnSuccessListener { barcode ->
                            val rawValue = barcode.rawValue
                            if (rawValue.isNullOrBlank()) {
                                viewModel.onBarcodeNotRecognized()
                            } else {
                                viewModel.findScannedProduct(rawValue, barcode.format.toBarcodeFormatName())
                            }
                        }.addOnCanceledListener { viewModel.onScanCancelled() }
                        .addOnFailureListener { exception -> viewModel.onScannerUnavailable(exception.toScannerMessage()) }
                },
                modifier = contentModifier,
            )
        } else {
            QuickAddContent(
                state = state,
                viewModel = viewModel,
                modifier = contentModifier,
            )
        }
    }

    ProductNotFoundDialog(
        pendingBarcode = if (state.openFoodFactsProduct == null) state.pendingScannedBarcode else null,
        isLoading = state.isOpenFoodFactsLoading,
        errorMessage = state.openFoodFactsError,
        onLookup = viewModel::lookupOpenFoodFacts,
        onCreate = { pending ->
            viewModel.dismissProductNotFound()
            onCreateProduct(pending.rawValue, pending.format, null)
        },
        onDismiss = viewModel::dismissProductNotFound,
    )

    OpenFoodFactsProductDialog(
        product = state.openFoodFactsProduct,
        onSaveAndAdd = viewModel::saveOpenFoodFactsProductAndAdd,
        onEdit = { product ->
            val pending = state.pendingScannedBarcode
            viewModel.dismissOpenFoodFactsProduct()
            onCreateProduct(pending?.rawValue, pending?.format, product)
        },
        onDismiss = viewModel::dismissOpenFoodFactsProduct,
    )
}

@Composable
private fun ProductPickerContent(
    state: AddMealUiState,
    viewModel: AddMealViewModel,
    onCreateProduct: () -> Unit,
    onCreateDish: () -> Unit,
    onSelectDish: (Long) -> Unit,
    onScanBarcode: () -> Unit,
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
                    text = "Выберите продукт или сохранённое блюдо. КБЖУ подтянутся автоматически.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ProductSearchField(state.searchQuery, viewModel::updateSearchQuery)
                FilledTonalButton(
                    onClick = onScanBarcode,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Сканировать штрихкод",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCreateProduct,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.58f),
                            ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Продукт", modifier = Modifier.padding(start = 6.dp))
                    }
                    Button(
                        onClick = onCreateDish,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = CtaColor,
                                contentColor = Color.White,
                            ),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(text = "Блюдо", modifier = Modifier.padding(start = 6.dp))
                    }
                }
                AddMealFilters(state, viewModel)
            }
        }

        item {
            Text(
                text = state.selectedFilter.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.filteredItems.isEmpty() && !state.isLoading) {
            item { EmptyState("Продукты или блюда не найдены") }
        } else {
            items(state.filteredItems, key = { "${it.product?.id ?: 0}-${it.dish?.id ?: 0}-${it.name}" }) { item ->
                item.product?.let { product ->
                    AddMealProductCard(
                        product = product,
                        showTypeBadge = item.showTypeBadge,
                        onSelect = { viewModel.selectProduct(product) },
                        onToggleFavorite = { viewModel.toggleProductFavorite(product) },
                    )
                }
                item.dish?.let { dish ->
                    AddMealDishCard(
                        dish = dish,
                        showTypeBadge = item.showTypeBadge,
                        onSelect = { onSelectDish(dish.id) },
                        onToggleFavorite = { viewModel.toggleDishFavorite(dish) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        placeholder = { Text("Поиск продукта или блюда") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    )
}

@Composable
private fun AddMealFilters(
    state: AddMealUiState,
    viewModel: AddMealViewModel,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 38.dp),
        ) {
            items(AddMealProductFilter.entries) { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter.title, style = MaterialTheme.typography.labelLarge) },
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CtaColor.copy(alpha = 0.28f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = state.selectedFilter == filter,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f),
                            selectedBorderColor = CtaColor.copy(alpha = 0.44f),
                        ),
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .width(34.dp)
                    .height(40.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        ),
                    ),
        )
    }
}

@Composable
private fun AddMealProductCard(
    product: Product,
    showTypeBadge: Boolean = false,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showTypeBadge) TypeBadge("Продукт")
                }
                Text(
                    text = product.nutritionLine(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSelect,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) { Text("Выбрать") }
                FavoriteIconButton(isFavorite = product.isFavorite, onClick = onToggleFavorite)
            }
        }
    }
}

@Composable
private fun AddMealDishCard(
    dish: Dish,
    showTypeBadge: Boolean = true,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .background(CtaColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = CtaColor,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dish.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showTypeBadge) TypeBadge("Блюдо")
                }
                Text(
                    "${dish.totalWeight.grams()} г · ${dish.calories.kcal()} ккал",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MacroLine(protein = dish.protein, fat = dish.fat, carbs = dish.carbs)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onSelect,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text("Выбрать")
                }
                FavoriteIconButton(isFavorite = dish.isFavorite, onClick = onToggleFavorite)
            }
        }
    }
}

@Composable
private fun TypeBadge(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .padding(start = 4.dp),
    )
}

@Composable
private fun AddMealSelectButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier =
            Modifier
                .height(34.dp)
            .defaultMinSize(minWidth = 74.dp, minHeight = 34.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = CtaColor.copy(alpha = 0.18f),
            contentColor = Color.White,
            ),
    ) {
        Text(
            text = "Выбрать",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun MacroLine(protein: Double, fat: Double, carbs: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("Б ${protein.grams()}", color = ProteinColor, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1)
        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1)
        Text("Ж ${fat.grams()}", color = FatColor, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1)
        Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1)
        Text("У ${carbs.grams()}", color = CarbsColor, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), maxLines = 1)
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
            modifier = Modifier
                .fillMaxWidth()
                .background(
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
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        product.nutritionLine(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
            PremiumCard(modifier = Modifier.fillMaxWidth()) {
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = CtaColor,
                        contentColor = Color.White,
                    ),
            ) { Text("Добавить") }
        }
    }
}

@Composable
private fun ProductNotFoundDialog(
    pendingBarcode: PendingScannedBarcode?,
    isLoading: Boolean,
    errorMessage: String?,
    onLookup: () -> Unit,
    onCreate: (PendingScannedBarcode) -> Unit,
    onDismiss: () -> Unit,
) {
    val pending = pendingBarcode ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Продукт не найден") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Этого кода нет в локальной базе. Попробовать найти продукт в Open Food Facts?")
                if (isLoading) Text("Ищем продукт…", color = MaterialTheme.colorScheme.primary)
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onLookup, enabled = !isLoading) { Text("Найти") } },
        dismissButton = {
            Row {
                TextButton(onClick = { onCreate(pending) }, enabled = !isLoading) { Text("Создать вручную") }
                TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Отмена") }
            }
        },
    )
}

@Composable
private fun OpenFoodFactsProductDialog(
    product: OpenFoodFactsProduct?,
    onSaveAndAdd: () -> Unit,
    onEdit: (OpenFoodFactsProduct) -> Unit,
    onDismiss: () -> Unit,
) {
    val foundProduct = product ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Продукт найден") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(foundProduct.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${foundProduct.caloriesPer100g.kcal()} ккал • Б ${foundProduct.proteinPer100g.grams()} • Ж ${foundProduct.fatPer100g.grams()} • У ${foundProduct.carbsPer100g.grams()} на 100 г",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onSaveAndAdd) { Text("Сохранить и добавить") } },
        dismissButton = {
            Row {
                TextButton(onClick = { onEdit(foundProduct) }) { Text("Редактировать") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        },
    )
}

private fun Int.toBarcodeFormatName(): String =
    when (this) {
        Barcode.FORMAT_CODE_128 -> "CODE_128"
        Barcode.FORMAT_CODE_39 -> "CODE_39"
        Barcode.FORMAT_CODE_93 -> "CODE_93"
        Barcode.FORMAT_CODABAR -> "CODABAR"
        Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_EAN_8 -> "EAN_8"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_QR_CODE -> "QR_CODE"
        Barcode.FORMAT_UPC_A -> "UPC_A"
        Barcode.FORMAT_UPC_E -> "UPC_E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "AZTEC"
        else -> "UNKNOWN"
    }

private fun Exception.toScannerMessage(): String {
    val statusCode = (this as? ApiException)?.statusCode
    return when (statusCode) {
        CommonStatusCodes.CANCELED -> "Сканирование отменено"

        ConnectionResult.SERVICE_MISSING,
        ConnectionResult.SERVICE_DISABLED,
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
        -> "Google Play services недоступны"

        CommonStatusCodes.ERROR,
        CommonStatusCodes.DEVELOPER_ERROR,
        CommonStatusCodes.INTERNAL_ERROR,
        CommonStatusCodes.API_NOT_CONNECTED,
        -> "Сканер недоступен"

        else -> "Сканер недоступен"
    }
}
