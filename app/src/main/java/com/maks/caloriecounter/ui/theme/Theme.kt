package com.maks.caloriecounter.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8B5FF),
    onPrimary = Color(0xFF20204A),
    primaryContainer = Color(0xFF34356F),
    onPrimaryContainer = Color(0xFFE4E1FF),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF0D0F14),
    onBackground = Color(0xFFE7E8EE),
    surface = Color(0xFF0D0F14),
    onSurface = Color(0xFFE7E8EE),
    surfaceContainer = Color(0xFF171A22),
    surfaceContainerHigh = Color(0xFF20242E),
    onSurfaceVariant = Color(0xFFB8BCC8),
    outlineVariant = Color(0xFF444957),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CalorieCounterTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+, but the app keeps a consistent dark UI by default.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}