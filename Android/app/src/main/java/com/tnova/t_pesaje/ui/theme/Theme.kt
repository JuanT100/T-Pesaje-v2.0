package com.tnova.t_pesaje.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores para modo oscuro.
private val ColorDarkBackground = Color(0xFF121212)
private val ColorDarkSurface = Color(0xFF1E1E1E)
private val ColorDarkText = Color(0xFFE0E0E0)

private val LightColorScheme = lightColorScheme(
    primary = TPesajeBlue,
    onPrimary = TPesajeSurface,

    secondary = TPesajeBlueDark,
    onSecondary = TPesajeSurface,

    tertiary = TPesajeMenuControl,
    onTertiary = TPesajeSurface,

    background = TPesajeBackground,
    onBackground = TPesajeText,

    surface = TPesajeSurface,
    onSurface = TPesajeText,

    error = TPesajeError,
    onError = TPesajeSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = TPesajeBlueLight,
    onPrimary = TPesajeText,

    secondary = TPesajeBlue,
    onSecondary = TPesajeSurface,

    tertiary = TPesajeMenuControl,
    onTertiary = TPesajeSurface,

    background = ColorDarkBackground,
    onBackground = ColorDarkText,

    surface = ColorDarkSurface,
    onSurface = ColorDarkText,

    error = TPesajeError,
    onError = TPesajeSurface
)

/**
 * Aplica el tema visual de T-Pesaje.
 */
@Composable
fun TPesajeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}