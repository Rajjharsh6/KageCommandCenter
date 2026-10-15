package com.kage.commandcenter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CharcoalBackground = Color(0xFF0F0F12)
val CharcoalSurface = Color(0xFF18181D)
val CharcoalSurfaceVariant = Color(0xFF22222B)
val CyanAccent = Color(0xFF00E5FF)
val VioletAccent = Color(0xFF8A2BE2)
val CriticalRed = Color(0xFFFF3366)
val SuccessGreen = Color(0xFF00E676)
val TextPrimary = Color(0xFFEEEEF0)
val TextSecondary = Color(0xFFA0A0AC)

private val KageColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = VioletAccent,
    background = CharcoalBackground,
    surface = CharcoalSurface,
    surfaceVariant = CharcoalSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = CriticalRed
)

@Composable
fun KageTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KageColorScheme,
        content = content
    )
}