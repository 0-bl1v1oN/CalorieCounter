package com.maks.caloriecounter.ui.components

import java.util.Locale

fun Double.kcal(): String = String.format(Locale.US, "%.0f", this)
fun Double.grams(): String = if (this % 1.0 == 0.0) String.format(Locale.US, "%.0f", this) else String.format(Locale.US, "%.1f", this)
