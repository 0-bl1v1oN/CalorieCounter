package com.maks.caloriecounter.domain.model

enum class MealType(val title: String) {
    Breakfast("Завтрак"),
    Lunch("Обед"),
    Dinner("Ужин"),
    Snack("Перекус");

    companion object {
        fun fromStorage(value: String): MealType = entries.firstOrNull { it.name == value } ?: Snack
    }
}
