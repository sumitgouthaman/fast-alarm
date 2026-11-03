package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepBedsideDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = VoidBlack,
    primaryContainer = SlateElevated,
    onPrimaryContainer = NeonCyan,
    secondary = AmberWarm,
    onSecondary = VoidBlack,
    secondaryContainer = SlateElevated,
    onSecondaryContainer = AmberWarm,
    tertiary = ElectricBlue,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = FrostWhite,
    surface = SlateDark,
    onSurface = FrostWhite,
    surfaceVariant = SlateElevated,
    onSurfaceVariant = TextMuted,
    outline = SlateBorder,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeepBedsideDarkColorScheme,
        typography = Typography,
        content = content
    )
}
