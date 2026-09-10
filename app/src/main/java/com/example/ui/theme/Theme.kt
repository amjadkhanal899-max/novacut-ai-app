package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = NeonCyan,
    secondary = GoldAccent,
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF594400),
    onSecondaryContainer = GoldAccent,
    tertiary = ElectricVioletLight,
    onTertiary = Color.White,
    tertiaryContainer = ElectricViolet,
    onTertiaryContainer = Color(0xFFEADBFF),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF222230)
)

private val LightColorScheme = DarkColorScheme // Default to high-contrast cinematic dark theme

@Composable
fun NovaCutAITheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NovaCutAITheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
