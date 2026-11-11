package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkNovelColorScheme = darkColorScheme(
    primary = SbCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A1010),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = SbGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382C18),
    onSecondaryContainer = Color(0xFFFFDDB3),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

val LightNovelColorScheme = lightColorScheme(
    primary = SbCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = SbGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB3),
    onSecondaryContainer = Color(0xFF2A1800),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

val SepiaNovelColorScheme = lightColorScheme(
    primary = SepiaAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCBE),
    onPrimaryContainer = Color(0xFF321200),
    secondary = SbCrimson,
    onSecondary = Color.White,
    background = SepiaBackground,
    onBackground = SepiaTextPrimary,
    surface = SepiaSurface,
    onSurface = SepiaTextPrimary,
    surfaceVariant = SepiaSurfaceVariant,
    onSurfaceVariant = SepiaTextSecondary,
    outline = SepiaBorder
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "dark", // "dark", "light", "sepia", "system"
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        "light" -> LightNovelColorScheme
        "sepia" -> SepiaNovelColorScheme
        "system" -> if (systemDark) DarkNovelColorScheme else LightNovelColorScheme
        else -> DarkNovelColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
