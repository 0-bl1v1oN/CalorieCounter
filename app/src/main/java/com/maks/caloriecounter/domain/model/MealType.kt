package com.maks.caloriecounter.domain.model

enum class MealType(val title: String) {
    Breakfast("Завтрак"),
    Lunch("Обед"),
    Dinner("Ужин"),
    Snack("Перекус");

    companion object {
        val todaySections: List<MealType> = listOf(Breakfast, Lunch, Dinner, Snack)

        fun fromStorage(value: String): MealType = entries.firstOrNull { type ->
            type.name.equals(value, ignoreCase = true) || type.title.equals(value, ignoreCase = true)
        } ?: Snack
    }
}
