package com.maks.caloriecounter.ui.screens.today

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maks.caloriecounter.domain.model.DailySummary
import com.maks.caloriecounter.domain.model.MealType
import com.maks.caloriecounter.domain.model.UserSettings
import com.maks.caloriecounter.domain.util.DateUtils
import com.maks.caloriecounter.ui.components.MealEntryCard
import com.maks.caloriecounter.ui.components.grams
import com.maks.caloriecounter.ui.components.kcal

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
            contentPadding = PaddingValues(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                TodayHeader(
                    date = state.date,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                )
            }
            item { DailyProgressCard(summary = state.summary, settings = state.settings) }
            item { AddFoodButton(onClick = onAddMeal) }
            item {
                Text(
                    text = "Сегодняшние приёмы пищи",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (state.entries.isEmpty()) {
                item { EmptyMealSection("Добавьте первый продукт за сегодня") }
            } else {
                MealType.todaySections.forEach { mealType ->
                    val mealEntries = entriesByMeal[mealType].orEmpty()
                    if (mealEntries.isNotEmpty()) {
                        item { MealSectionTitle(title = mealType.title) }
                        items(mealEntries, key = { it.entry.id }) { details ->
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
            .size(58.dp)
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
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
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(148.dp),
            strokeWidth = 14.dp,
            color = PinkSoft.copy(alpha = 0.15f),
            trackColor = Color.Transparent,
        )
        CircularProgressIndicator(
            progress = { calorieProgress },
            modifier = Modifier.size(148.dp),
            strokeWidth = 14.dp,
            color = TodayPink,
            trackColor = Color.Transparent,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("🔥", style = MaterialTheme.typography.titleLarge)
            Text("ККАЛ", style = MaterialTheme.typography.labelLarge, color = MutedText)
            Text(summary.calories.kcal(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = Color.White)
            Text("/ ${settings.calorieGoal}", style = MaterialTheme.typography.titleMedium, color = MutedText)
            Text("ккал", style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
    }
}

@Composable
private fun RemainingCalories(summary: DailySummary, settings: UserSettings, modifier: Modifier = Modifier) {
    val remaining = (settings.calorieGoal - summary.calories).coerceAtLeast(0.0)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Осталось", style = MaterialTheme.typography.titleMedium, color = MutedText)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(remaining.kcal(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = TodayPink)
            Spacer(Modifier.width(6.dp))
            Text("ккал", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TodayPink)
        }
        LinearProgressIndicator(
            progress = { progress(summary.calories, settings.calorieGoal.toDouble()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = TodayPink,
            trackColor = PinkSoft.copy(alpha = 0.14f),
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MacroProgressRow("Белки", "Цель: ${settings.proteinGoal} г", summary.protein, settings.proteinGoal, TodayPink)
            MacroProgressRow("Жиры", "Цель: ${settings.fatGoal} г", summary.fat, settings.fatGoal, Amber)
            MacroProgressRow("Углеводы", "Цель: ${settings.carbsGoal} г", summary.carbs, settings.carbsGoal, PinkSoft)
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
) {
    val percent = if (goal <= 0) 0 else ((value / goal) * 100).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(color.copy(alpha = 0.14f))
                .border(BorderStroke(1.dp, color.copy(alpha = 0.38f)), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(title.take(1), style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedText)
                }
                Text("${value.grams()} / $goal г", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            LinearProgressIndicator(
                progress = { progress(value, goal.toDouble()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text("$percent%", color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AddFoodButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFF6C9A), Color(0xFFE92561))))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Добавить еду", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.74f))
        }
    }
}

@Composable
private fun MealSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@Composable
private fun EmptyMealSection(text: String = "Нет продуктов") {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MutedText,
            modifier = Modifier.padding(18.dp),
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
        shadowElevation = 0.dp,
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
private val TodayPink = Color(0xFFFF6C9A)
private val PinkSoft = Color(0xFFFF8BB1)
private val Amber = Color(0xFFFFA51F)
private val MutedText = Color(0xFFA7A9B2)
private val GlassDark = Color.White.copy(alpha = 0.055f)
private val GlassStroke = Color.White.copy(alpha = 0.12f)