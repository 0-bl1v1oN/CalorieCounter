package com.maks.caloriecounter.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private const val STORAGE_PATTERN = "yyyy-MM-dd"
    private val storageFormat = SimpleDateFormat(STORAGE_PATTERN, Locale.US)
    private val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("ru"))

    fun today(): String = storageFormat.format(Calendar.getInstance().time)

    fun shift(date: String, days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.time = requireNotNull(storageFormat.parse(date))
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return storageFormat.format(calendar.time)
    }

    fun display(date: String): String = runCatching {
        displayFormat.format(requireNotNull(storageFormat.parse(date)))
    }.getOrDefault(date)
}
