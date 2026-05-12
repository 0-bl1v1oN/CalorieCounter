package com.maks.caloriecounter.ui.screens.today

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.maks.caloriecounter.R
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
            contentPadding = PaddingValues(start = 16.dp, top = 22.dp, end = 16.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    color = PrimaryText,
                    modifier = Modifier.padding(top = 2.dp),
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
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(152.dp)) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(152.dp),
            strokeWidth = 13.dp,
            color = TodayAccent.copy(alpha = 0.14f),
            trackColor = Color.Transparent,
        )
        CircularProgressIndicator(
            progress = { calorieProgress },
            modifier = Modifier.size(152.dp),
            strokeWidth = 13.dp,
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
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 40.sp),
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Осталось", style = MaterialTheme.typography.titleMedium, color = MutedText)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(remaining.kcal(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = TodayAccent)
            Spacer(Modifier.width(6.dp))
            Text("ккал", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TodayAccent)
        }
        LinearProgressIndicator(
            progress = { progress(summary.calories, settings.calorieGoal.toDouble()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MacroProgressRow(
                title = "Белки",
                subtitle = "Цель: ${settings.proteinGoal} г",
                value = summary.protein,
                goal = settings.proteinGoal,
                color = TodayPink,
                iconRes = R.drawable.today_macro_protein,
            )
            MacroProgressRow(
                title = "Жиры",
                subtitle = "Цель: ${settings.fatGoal} г",
                value = summary.fat,
                goal = settings.fatGoal,
                color = Amber,
                iconRes = R.drawable.today_macro_fat,
            )
            MacroProgressRow(
                title = "Углеводы",
                subtitle = "Цель: ${settings.carbsGoal} г",
                value = summary.carbs,
                goal = settings.carbsGoal,
                color = PinkSoft,
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.10f))
                .border(BorderStroke(1.dp, color.copy(alpha = 0.20f)), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier.size(30.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "${value.grams()} / $goal г",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryText,
                    maxLines = 1,
                    softWrap = false,
                )
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
                .padding(horizontal = 9.dp, vertical = 7.dp),
        ) {
            Text("$percent%", color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
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
            .background(Brush.horizontalGradient(listOf(TodayAccent, TodayAccentDark)))
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
private val TodayAccent = Color(0xFFFF6C9A)
private val TodayAccentDark = Color(0xFFE92561)
private val TodayPink = Color(0xFFFF6C9A)
private val PinkSoft = Color(0xFFFF8BB1)
private val Amber = Color(0xFFFFA51F)
private val PrimaryText = Color(0xFFF4F6FA)
private val MutedText = Color(0xFFA7A9B2)
private val GlassDark = Color.White.copy(alpha = 0.055f)
private val GlassStroke = Color.White.copy(alpha = 0.12f)