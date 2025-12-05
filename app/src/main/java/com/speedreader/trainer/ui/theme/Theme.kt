package com.speedreader.trainer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Teal300,
    onPrimary = Charcoal,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal100,
    secondary = Amber300,
    onSecondary = Charcoal,
    secondaryContainer = Amber700,
    onSecondaryContainer = Amber300,
    tertiary = Teal500,
    onTertiary = Charcoal,
    background = Charcoal,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Error,
    onError = CardLight
)

private val LightColorScheme = lightColorScheme(
    primary = Teal700,
    onPrimary = CardLight,
    primaryContainer = Teal100,
    onPrimaryContainer = Teal900,
    secondary = Amber500,
    onSecondary = Charcoal,
    secondaryContainer = Amber300,
    onSecondaryContainer = Charcoal,
    tertiary = Teal500,
    onTertiary = CardLight,
    background = LightSurface,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurface,
    onSurfaceVariant = TextSecondaryLight,
    error = Error,
    onError = CardLight
)

@Composable
fun SpeedReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

