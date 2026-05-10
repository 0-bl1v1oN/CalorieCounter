package com.maks.caloriecounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.maks.caloriecounter.di.AppContainer
import com.maks.caloriecounter.ui.navigation.AppNavigation
import com.maks.caloriecounter.ui.theme.CalorieCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            CalorieCounterTheme {
                AppNavigation(appContainer = appContainer)
            }
        }
    }
}

