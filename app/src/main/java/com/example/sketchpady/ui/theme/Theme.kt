package com.example.sketchpady.ui.theme

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sketchpady.data.dataStore
import com.example.sketchpady.data.isDarkThemeOn
import com.example.sketchpady.data.themePreferenceKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val DarkColorScheme = darkColorScheme(
    primary = WhiteDark,
    secondary = Accent,
    background = BlackDark,
    surface = BlackLite,
    onPrimary = BlackDark,
    onSecondary = BlackDark,
    onBackground = WhiteLite,
    onSurface = WhiteDark,
)

private val LightColorScheme = lightColorScheme(
    primary = BlackDark,
    secondary = Accent,
    background = WhiteDark,
    surface = WhiteLite,
    onPrimary = WhiteDark,
    onSecondary = WhiteDark,
    onBackground = BlackDark,
    onSurface = BlackDark,
)

@Composable
fun SketchPadyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

@Composable
fun isSystemInDarkThemeCustom(): Boolean {
    val context = LocalContext.current
    val exampleData = runBlocking { context.dataStore.data.first() }
    val theme = context.isDarkThemeOn().collectAsState(initial = exampleData[themePreferenceKey] ?: 0)
    return when (theme.value) {
        2 -> true
        1 -> false
        else -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}

@Composable
fun Window.StatusBarConfig(darkTheme: Boolean) {
    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars =
        !darkTheme
    this.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
}