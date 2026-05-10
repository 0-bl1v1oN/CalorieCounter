package com.maks.caloriecounter.ui.navigation

data class BottomNavItem(val route: String, val label: String)

val bottomNavItems = listOf(
    BottomNavItem(Routes.Today, "Сегодня"),
    BottomNavItem(Routes.Products, "Продукты"),
    BottomNavItem(Routes.History, "История"),
    BottomNavItem(Routes.Settings, "Настройки"),
)
