package com.maks.caloriecounter.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.maks.caloriecounter.data.local.AppDatabase
import com.maks.caloriecounter.data.preferences.UserPreferencesDataStore
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.data.repository.SettingsRepository
import com.maks.caloriecounter.ui.screens.addmeal.AddMealViewModel
import com.maks.caloriecounter.ui.screens.history.HistoryViewModel
import com.maks.caloriecounter.ui.screens.products.ProductsViewModel
import com.maks.caloriecounter.ui.screens.settings.SettingsViewModel
import com.maks.caloriecounter.ui.screens.today.TodayViewModel

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, "calorie_counter.db").build()

    val productRepository = ProductRepository(database.productDao())
    val mealRepository = MealRepository(database.mealEntryDao())
    val settingsRepository = SettingsRepository(UserPreferencesDataStore(appContext))

    fun todayViewModelFactory(date: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TodayViewModel(date, mealRepository, settingsRepository) as T
    }

    fun addMealViewModelFactory(date: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AddMealViewModel(date, productRepository, mealRepository) as T
    }

    fun productsViewModelFactory(date: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProductsViewModel(date, productRepository, mealRepository) as T
    }

    fun historyViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(mealRepository) as T
    }

    fun settingsViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(settingsRepository) as T
    }
}
