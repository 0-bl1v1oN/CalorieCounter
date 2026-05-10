package com.maks.caloriecounter.domain.model

data class UserSettings(
    val calorieGoal: Int = 2200,
    val proteinGoal: Int = 150,
    val fatGoal: Int = 70,
    val carbsGoal: Int = 250,
)
