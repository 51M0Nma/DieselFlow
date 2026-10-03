package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DieselPrimary,
    onPrimary = DieselOnError,
    primaryContainer = DieselPrimaryContainer,
    onPrimaryContainer = DieselOnPrimaryContainer,
    inversePrimary = DieselInversePrimary,
    secondary = DieselSecondary,
    onSecondary = DieselOnError,
    secondaryContainer = DieselSecondaryContainer,
    onSecondaryContainer = DieselOnSecondaryContainer,
    tertiary = DieselTertiary,
    onTertiary = DieselOnError,
    tertiaryContainer = DieselTertiaryContainer,
    onTertiaryContainer = DieselOnTertiaryContainer,
    background = DieselSurface,
    onBackground = DieselOnSurface,
    surface = DieselSurface,
    onSurface = DieselOnSurface,
    surfaceVariant = DieselSurfaceVariant,
    onSurfaceVariant = DieselOnSurfaceVariant,
    surfaceTint = DieselPrimaryVariant,
    inverseSurface = DieselInverseSurface,
    inverseOnSurface = DieselInverseOnSurface,
    outline = DieselOutline,
    outlineVariant = DieselOutlineVariant,
    error = DieselError,
    onError = DieselOnError,
    errorContainer = DieselErrorContainer,
    onErrorContainer = DieselOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DieselPrimaryFixedDim,
    onPrimary = DieselOnPrimaryFixed,
    primaryContainer = DieselPrimaryVariant,
    onPrimaryContainer = DieselPrimaryFixed,
    inversePrimary = DieselPrimary,
    secondary = DieselSecondaryFixedDim,
    onSecondary = DieselOnSecondaryFixed,
    secondaryContainer = DieselOnSecondaryFixedVariant,
    onSecondaryContainer = DieselSecondaryFixed,
    tertiary = DieselTertiaryFixedDim,
    onTertiary = DieselOnTertiaryFixed,
    tertiaryContainer = DieselTertiary,
    onTertiaryContainer = DieselTertiaryFixed,
    background = DieselInverseSurface,
    onBackground = DieselInverseOnSurface,
    surface = DieselInverseSurface,
    onSurface = DieselInverseOnSurface,
    surfaceVariant = DieselSecondary,
    onSurfaceVariant = DieselSecondaryFixedDim,
    outline = DieselOutlineVariant,
    error = DieselError,
    onError = DieselOnError
)

@Composable
fun DieselFlowTheme(
    darkTheme: Boolean = false, // Keep high visibility industrial theme default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
