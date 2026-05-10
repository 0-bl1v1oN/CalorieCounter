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

    fun todayViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        TodayViewModel(date, mealRepository, settingsRepository)
    }

    fun addMealViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        AddMealViewModel(date, productRepository, mealRepository)
    }

    fun productsViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        ProductsViewModel(date, productRepository, mealRepository)
    }

    fun historyViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        HistoryViewModel(mealRepository)
    }

    fun settingsViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        SettingsViewModel(settingsRepository)
    }
}

private inline fun <reified VM : ViewModel> viewModelFactory(crossinline createViewModel: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VM::class.java)) {
                return requireNotNull(modelClass.cast(createViewModel()))
            }
            error("Unknown ViewModel class: ${modelClass.name}")
        }
    }