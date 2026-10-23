package com.akash.camdetector.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkScheme = darkColorScheme(
    background = Color(0xFF0B1220),
    surface = Color(0xFF121D31),
    surfaceVariant = Color(0xFF0E1729),
    primary = Color(0xFF22D3EE),
    onPrimary = Color(0xFF04222A),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF05281C),
    tertiary = Color(0xFFFB923C),
    onTertiary = Color(0xFF3B1A02),
    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFEAF2FF),
    onSurfaceVariant = Color(0xFF8FA3C4),
    outline = Color(0xFF1D2C47)
)

val LightScheme = lightColorScheme(
    background = Color(0xFFF4F6FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEFF3FA),
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF16A34A),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFEA580C),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF16233B),
    onSurface = Color(0xFF16233B),
    onSurfaceVariant = Color(0xFF7A8AA6),
    outline = Color(0xFFE6EAF3)
)

@Composable
fun CamTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
