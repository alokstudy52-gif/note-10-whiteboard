package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Note10DarkScheme = darkColorScheme(
    primary = NoteLeafGreen,
    onPrimary = Color(0xFF0E2107),
    primaryContainer = NoteForestPrimary,
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondary = AccentAmber,
    onSecondary = Color(0xFF281700),
    secondaryContainer = Color(0xFF4B3200),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = AccentCyan,
    onTertiary = Color(0xFF00252C),
    background = DockCharcoal,
    onBackground = DockOnSurface,
    surface = DockSurface,
    onSurface = DockOnSurface,
    surfaceVariant = DockSurfaceElevated,
    onSurfaceVariant = DockOnSurfaceMuted,
    outline = DockBorder,
    error = AccentCoral,
    onError = Color.White
)

private val Note10LightScheme = lightColorScheme(
    primary = NoteForestPrimary,
    onPrimary = Color.White,
    primaryContainer = NoteMintTint,
    onPrimaryContainer = NoteForestDark,
    secondary = AccentAmber,
    onSecondary = Color(0xFF281700),
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AccentIndigo,
    onTertiary = Color.White,
    background = Color(0xFFF6F8F5),
    onBackground = Color(0xFF141B14),
    surface = Color.White,
    onSurface = Color(0xFF141B14),
    surfaceVariant = Color(0xFFEDF2EC),
    onSurfaceVariant = Color(0xFF475447),
    outline = Color(0xFFC8D4C7),
    error = AccentCoral,
    onError = Color.White
)

@Composable
fun Note10Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) Note10DarkScheme else Note10LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
