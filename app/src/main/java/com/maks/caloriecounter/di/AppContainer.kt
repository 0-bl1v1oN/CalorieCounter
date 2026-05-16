package com.maks.caloriecounter.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.maks.caloriecounter.data.importer.ProductCsvImporter
import com.maks.caloriecounter.data.local.AppDatabase
import com.maks.caloriecounter.data.preferences.UserPreferencesDataStore
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsApi
import com.maks.caloriecounter.data.remote.openfoodfacts.OpenFoodFactsRemoteDataSource
import com.maks.caloriecounter.data.repository.DishRepository
import com.maks.caloriecounter.data.repository.MealRepository
import com.maks.caloriecounter.data.repository.ProductRepository
import com.maks.caloriecounter.data.repository.SettingsRepository
import com.maks.caloriecounter.ui.screens.addmeal.AddMealViewModel
import com.maks.caloriecounter.ui.screens.dishes.DishFormViewModel
import com.maks.caloriecounter.ui.screens.dishes.DishLogViewModel
import com.maks.caloriecounter.ui.screens.dishes.ProductPickerViewModel
import com.maks.caloriecounter.ui.screens.history.HistoryViewModel
import com.maks.caloriecounter.ui.screens.products.ProductsViewModel
import com.maks.caloriecounter.ui.screens.settings.SettingsViewModel
import com.maks.caloriecounter.ui.screens.today.TodayViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: AppDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, "calorie_counter.db")
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .build()

    private val productDao = database.productDao()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val productRepository = ProductRepository(productDao)
    val dishRepository = DishRepository(database.dishDao())
    val mealRepository = MealRepository(database.mealEntryDao())
    val settingsRepository = SettingsRepository(UserPreferencesDataStore(appContext))

    private val openFoodFactsClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("User-Agent", "CalorieCounter/1.0 (Android; personal app)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val openFoodFactsApi: OpenFoodFactsApi = Retrofit.Builder()
        .baseUrl(OpenFoodFactsApi.BASE_URL)
        .client(openFoodFactsClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenFoodFactsApi::class.java)

    private val openFoodFactsRemoteDataSource = OpenFoodFactsRemoteDataSource(openFoodFactsApi)
    init {
        applicationScope.launch {
            ProductCsvImporter(appContext, productDao).importIfNeeded()
        }
    }

    fun todayViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        TodayViewModel(date, mealRepository, settingsRepository)
    }

    fun addMealViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        ddMealViewModel(date, productRepository, dishRepository, mealRepository, openFoodFactsRemoteDataSource)
    }

    fun productsViewModelFactory(date: String): ViewModelProvider.Factory = viewModelFactory {
        ProductsViewModel(date, productRepository, dishRepository, mealRepository)
    }

    fun dishFormViewModelFactory(dishId: Long? = null): ViewModelProvider.Factory = viewModelFactory {
        DishFormViewModel(dishId, dishRepository, productRepository)
    }

    fun productPickerViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        ProductPickerViewModel(productRepository)
    }

    fun dishLogViewModelFactory(date: String, dishId: Long): ViewModelProvider.Factory = viewModelFactory {
        DishLogViewModel(date, dishId, dishRepository, mealRepository)
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