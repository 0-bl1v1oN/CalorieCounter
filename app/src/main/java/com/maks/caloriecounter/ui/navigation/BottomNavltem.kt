package com.maks.caloriecounter.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Routes.Today, "Сегодня", Icons.Outlined.Today),
    BottomNavItem(Routes.Products, "Продукты", Icons.Outlined.Restaurant),
    BottomNavItem(Routes.History, "История", Icons.Outlined.History),
    BottomNavItem(Routes.Settings, "Настройки", Icons.Outlined.Settings),
)
