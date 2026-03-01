package com.assistant.voip.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColors
import androidx.compose.material3.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 主题颜色
val PrimaryBlue = Color(0xFF2196F3)
val SecondaryBlue = Color(0xFF1976D2)
val TertiaryBlue = Color(0xFF0D47A1)

val AccentBlue = Color(0xFF64B5F6)
val LightBlue = Color(0xFFBBDEFB)
val VeryLightBlue = Color(0xFFE3F2FD)

val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFF44336)

val BackgroundLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF121212)

val SurfaceLight = Color(0xFFF5F5F5)
val SurfaceDark = Color(0xFF1E1E1E)

private val LightColorPalette = lightColors(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = TertiaryBlue,
    primaryContainer = AccentBlue,
    onPrimary = Color.White,
    secondaryContainer = LightBlue,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = Color.Black,
    surface = SurfaceLight,
    onSurface = Color.Black,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorPalette = darkColors(
    primary = AccentBlue,
    secondary = LightBlue,
    tertiary = VeryLightBlue,
    primaryContainer = PrimaryBlue,
    onPrimary = Color.White,
    secondaryContainer = SecondaryBlue,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun VoipAssistantTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
