package com.maks.caloriecounter.domain.model

data class DailySummary(
    val date: String,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
)
