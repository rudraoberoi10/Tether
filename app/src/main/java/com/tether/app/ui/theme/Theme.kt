package com.tether.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TetherDarkColorScheme = darkColorScheme(
    primary = TetherTeal,
    onPrimary = TetherBg,
    primaryContainer = TetherSurfaceElevated,
    onPrimaryContainer = TetherTeal,
    secondary = TetherTeal,
    onSecondary = TetherBg,
    background = TetherBg,
    onBackground = TextPrimary,
    surface = TetherSurface,
    onSurface = TextPrimary,
    surfaceVariant = TetherSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = TetherCoral,
    onError = TextPrimary
)

val TetherShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp) // PRD 24dp card & sheet radius
)

@Composable
fun TetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TetherDarkColorScheme,
        typography = Typography,
        shapes = TetherShapes,
        content = content
    )
}