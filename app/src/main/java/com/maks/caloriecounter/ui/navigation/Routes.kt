package com.maks.caloriecounter.ui.navigation

object Routes {
    const val Today = "today"
    const val Products = "products"
    const val ProductAdd = "products/add"
    const val ProductEdit = "products/edit/{productId}"
    const val History = "history"
    const val Settings = "settings"
    const val AddMeal = "addMeal"

    fun productEdit(productId: Long): String = "products/edit/$productId"
}
