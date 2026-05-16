package com.maks.caloriecounter.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.maks.caloriecounter.R
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealEntryDetails
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.UserSettings
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.components.MealEntryCard
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal
import kotlin.math.max

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarMessage: String? = null,
    onSnackbarShown: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val entriesByMeal = state.entries.groupBy { it.entry.mealType }
    val snackbarHostState = remember { SnackbarHostState() }
    var collapsedMealNames by rememberSaveable { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onSnackbarShown()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TodayBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TodayBackgroundBrush)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                TodayHeader(
                    date = state.date,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                )
            }
            item { DailyProgressCard(summary = state.summary, settings = state.settings) }
            item { WeeklyCaloriesCard(points = state.weekCalories) }
            item { AddFoodButton(onClick = onAddMeal) }
            item {
                Text(
                    text = "Сегодняшние приёмы пищи",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            MealType.todaySections.forEach { mealType ->
                val mealEntries = entriesByMeal[mealType].orEmpty()
                val isExpanded = mealType.name !in collapsedMealNames
                item(key = "meal-section-${mealType.name}") {
                    CollapsibleMealSection(
                        title = mealType.title,
                        entries = mealEntries,
                        isExpanded = isExpanded,
                        onToggle = {
                            collapsedMealNames = if (isExpanded) {
                                collapsedMealNames + mealType.name
                            } else {
                                collapsedMealNames - mealType.name
                            }
                        },
                    ) {
                        mealEntries.forEach { details ->
                            MealEntryCard(
                                entry = details,
                                onDelete = { viewModel.deleteEntry(details.entry.id) },
                                onUpdateGrams = { grams -> viewModel.updateGrams(details.entry.id, grams) },
                            )
                        }
                    }
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (date == DateUtils.today()) "Сегодня" else "Дневник",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp),
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp,
            )
            Text(
                text = DateUtils.displayDayWithWeekday(date),
                style = MaterialTheme.typography.titleMedium,
                color = MutedText,
            )
        }
        RoundDayButton(onClick = onPreviousDay) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
        }
        RoundDayButton(onClick = onNextDay) {
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun RoundDayButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(GlassDark)
            .border(BorderStroke(1.dp, GlassStroke), CircleShape),
    ) { content() }
}

@Composable
private fun DailyProgressCard(
    summary: DailySummary,
    settings: UserSettings,
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CalorieRing(summary = summary, settings = settings)
                RemainingCalories(summary = summary, settings = settings, modifier = Modifier.weight(1f))
            }
            MacroGlassList(summary = summary, settings = settings)
        }
    }
}

@Composable
private fun CalorieRing(summary: DailySummary, settings: UserSettings) {
    val calorieProgress = progress(summary.calories, settings.calorieGoal.toDouble())
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(126.dp)) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(126.dp),
            strokeWidth = 10.dp,
            color = TodayAccent.copy(alpha = 0.14f),
            trackColor = Color.Transparent,
        )
        CircularProgressIndicator(
            progress = { calorieProgress },
            modifier = Modifier.size(126.dp),
            strokeWidth = 10.dp,
            color = TodayAccent,
            trackColor = Color.Transparent,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Image(
                painter = painterResource(R.drawable.today_calories_fire),
                contentDescription = "Калории",
                modifier = Modifier.size(18.dp),
            )
            Text("ККАЛ", style = MaterialTheme.typography.labelMedium, color = MutedText)
            Text(
                summary.calories.kcal(),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 31.sp, lineHeight = 33.sp),
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
            )
            Text("/ ${settings.calorieGoal}", style = MaterialTheme.typography.titleMedium, color = MutedText)
            Text("ккал", style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
    }
}

@Composable
private fun RemainingCalories(summary: DailySummary, settings: UserSettings, modifier: Modifier = Modifier) {
    val remaining = (settings.calorieGoal - summary.calories).coerceAtLeast(0.0)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Осталось", style = MaterialTheme.typography.titleMedium, color = MutedText)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(remaining.kcal(), style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp), fontWeight = FontWeight.Black, color = TodayAccent)
            Spacer(Modifier.width(6.dp))
            Text("ккал", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TodayAccent)
        }
        LinearProgressIndicator(
            progress = { progress(summary.calories, settings.calorieGoal.toDouble()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = TodayAccent,
            trackColor = TodayAccent.copy(alpha = 0.13f),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", color = MutedText, style = MaterialTheme.typography.bodyMedium)
            Text(settings.calorieGoal.toString(), color = MutedText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MacroGlassList(summary: DailySummary, settings: UserSettings) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.055f), Color.White.copy(alpha = 0.025f))),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            MacroProgressRow(
                title = "Белки",
                subtitle = "Цель: ${settings.proteinGoal} г",
                value = summary.protein,
                goal = settings.proteinGoal,
                color = ProteinLavender,
                iconRes = R.drawable.today_macro_protein,
            )
            MacroProgressRow(
                title = "Жиры",
                subtitle = "Цель: ${settings.fatGoal} г",
                value = summary.fat,
                goal = settings.fatGoal,
                color = FatAmber,
                iconRes = R.drawable.today_macro_fat,
            )
            MacroProgressRow(
                title = "Углеводы",
                subtitle = "Цель: ${settings.carbsGoal} г",
                value = summary.carbs,
                goal = settings.carbsGoal,
                color = CarbsRose,
                iconRes = R.drawable.today_macro_carbs,
            )
        }
    }
}

@Composable
private fun MacroProgressRow(
    title: String,
    subtitle: String,
    value: Double,
    goal: Int,
    color: Color,
    iconRes: Int,
) {
    val percent = if (goal <= 0) 0 else ((value / goal) * 100).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(color.copy(alpha = 0.10f))
                .border(BorderStroke(1.dp, color.copy(alpha = 0.20f)), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = color,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "${value.grams()} / $goal г",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryText,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            LinearProgressIndicator(
                progress = { progress(value, goal.toDouble()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = color,
                trackColor = Color.White.copy(alpha = 0.09f),
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.14f))
                .border(BorderStroke(1.dp, color.copy(alpha = 0.22f)), RoundedCornerShape(16.dp))
                .padding(horizontal = 7.dp, vertical = 5.dp),
        ) {
            Text("$percent%", color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun WeeklyCaloriesCard(points: List<WeeklyCaloriesPoint>) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = Brush.linearGradient(
            listOf(
                Color(0xFF171B24).copy(alpha = 0.94f),
                Color(0xFF0E1118).copy(alpha = 0.98f),
            ),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Калории за неделю",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TodayAccent.copy(alpha = 0.10f))
                        .border(BorderStroke(1.dp, TodayAccent.copy(alpha = 0.16f)), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Последние 7 дней",
                        style = MaterialTheme.typography.labelMedium,
                        color = TodayAccent,
                        maxLines = 1,
                    )
                }
            }
            WeeklyLineChart(points = points)
        }
    }
}

@Composable
private fun WeeklyLineChart(points: List<WeeklyCaloriesPoint>) {
    val safePoints = if (points.isEmpty()) List(7) { WeeklyCaloriesPoint(DateUtils.shift(DateUtils.today(), it - 6), 0.0) } else points
    val maxCalories = max(1.0, safePoints.maxOf { it.calories })

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
        ) {
            val horizontalStep = if (safePoints.size <= 1) size.width else size.width / (safePoints.lastIndex)
            val topPadding = 6f
            val bottomPadding = 8f
            val chartHeight = size.height - topPadding - bottomPadding
            val baseline = size.height - bottomPadding

            for (index in 0..3) {
                val y = topPadding + chartHeight * index / 3f
                drawLine(
                    color = Color.White.copy(alpha = 0.055f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val path = Path()
            safePoints.forEachIndexed { index, point ->
                val x = horizontalStep * index
                val y = baseline - (point.calories / maxCalories).toFloat() * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = TodayAccent,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
            safePoints.forEachIndexed { index, point ->
                val x = horizontalStep * index
                val y = baseline - (point.calories / maxCalories).toFloat() * chartHeight
                drawCircle(
                    color = TodayAccent.copy(alpha = if (point.calories > 0.0) 0.95f else 0.38f),
                    radius = 2.8.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            safePoints.forEach { point ->
                Text(
                    text = DateUtils.display(point.date).take(5),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MutedText,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun AddFoodButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AddButtonGlow)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Добавить еду",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun CollapsibleMealSection(
    title: String,
    entries: List<MealEntryDetails>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val totals = entries.sectionTotals()

    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            MealSectionHeader(
                title = title,
                totals = totals,
                isExpanded = isExpanded,
                onToggle = onToggle,
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (entries.isEmpty()) {
                        EmptyMealSection()
                    } else {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSectionHeader(
    title: String,
    totals: MealSectionTotals,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${totals.calories.kcal()} ккал",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TodayAccent,
                maxLines = 1,
                softWrap = false,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MutedText,
                modifier = Modifier.size(24.dp),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionMacroText(label = "Б", value = totals.protein, color = ProteinLavender, modifier = Modifier.weight(1f))
            SectionMacroText(label = "Ж", value = totals.fat, color = FatAmber, modifier = Modifier.weight(1f))
            SectionMacroText(label = "У", value = totals.carbs, color = CarbsRose, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionMacroText(
    label: String,
    value: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$label ${value.grams()}",
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
    )
}

private data class MealSectionTotals(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
)

private fun List<MealEntryDetails>.sectionTotals(): MealSectionTotals = MealSectionTotals(
    calories = sumOf { it.calories },
    protein = sumOf { it.protein },
    fat = sumOf { it.fat },
    carbs = sumOf { it.carbs },
)

@Composable
private fun EmptyMealSection(text: String = "Нет продуктов") {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    colors: Brush = Brush.linearGradient(listOf(Color(0xFF181C24).copy(alpha = 0.94f), Color(0xFF0F1218).copy(alpha = 0.96f))),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .background(colors)
            .border(BorderStroke(1.dp, GlassStroke), shape),
        color = Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) { content() }
}

private fun progress(value: Double, goal: Double): Float {
    if (goal <= 0) return 0f
    return (value / goal).toFloat().coerceIn(0f, 1f)
}

private val TodayBackground = Color(0xFF05070A)
private val TodayBackgroundBrush = Brush.verticalGradient(
    listOf(
        Color(0xFF020305),
        Color(0xFF081017),
        Color(0xFF05070A),
    ),
)
private val TodayAccent = Color(0xFFFF6F9F)
private val TodayAccentDark = Color(0xFF9E6BFF)
private val ProteinLavender = Color(0xFF9FB2FF)
private val FatAmber = Color(0xFFFFB020)
private val CarbsRose = Color(0xFFFF5C9A)
private val PrimaryText = Color(0xFFF4F6FA)
private val MutedText = Color(0xFFA7A9B2)
private val GlassDark = Color.White.copy(alpha = 0.055f)
private val GlassStroke = Color.White.copy(alpha = 0.12f)
private val AddButtonGlow = Brush.horizontalGradient(listOf(TodayAccent.copy(alpha = 0.94f), TodayAccentDark.copy(alpha = 0.88f)))