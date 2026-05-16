package com.maks.caloriecounter.domain.model

enum class FoodLogEntryType(val storageValue: String) {
    Product("product"),
    Dish("dish");

    companion object {
        fun fromStorage(value: String): FoodLogEntryType = entries.firstOrNull { it.storageValue == value } ?: Product
    }
}
