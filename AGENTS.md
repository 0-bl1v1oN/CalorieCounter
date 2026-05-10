Project: CalorieCounter

This is a personal local-only Android calorie counter app.

Package:
com.maks.caloriecounter

Development environment:
- Windows
- VS Code
- Android Gradle project
- Use Gradle wrapper commands

Build command:
.\gradlew.bat assembleDebug

Rules:
- Use Kotlin.
- Use Jetpack Compose.
- Use Material 3.
- Use Room for local database.
- Use DataStore Preferences for settings.
- Use Navigation Compose.
- Use Coroutines and Flow.
- No Firebase.
- No Supabase.
- No accounts.
- No server.
- No required internet.
- No android.permission.INTERNET.
- No XML layouts.
- No Hilt, Dagger, or Koin.
- Use simple manual dependency wiring through AppContainer.
- Keep architecture simple and readable.
- Do not rename the package.
- Do not recreate the project from scratch.
- Do not modify Gradle unless needed for dependencies.
- Always verify with .\gradlew.bat assembleDebug after code changes.