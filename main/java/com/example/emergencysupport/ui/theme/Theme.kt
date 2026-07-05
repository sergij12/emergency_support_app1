package com.example.emergencysupport.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EmergencyColors = darkColorScheme(
    primary = RescueBlue,
    secondary = SafeMint,
    tertiary = SafetyAmber,
    background = MidnightNavy,
    surface = CardDark,
    surfaceVariant = DeepOcean,
    primaryContainer = DeepOcean,
    secondaryContainer = CardDark,
    tertiaryContainer = Color(0xFF3A2A10),
    onPrimary = MidnightNavy,
    onSecondary = MidnightNavy,
    onTertiary = MidnightNavy,
    onBackground = TextOnDark,
    onSurface = TextOnDark,
    onSurfaceVariant = TextOnDark,
    onPrimaryContainer = TextOnDark,
    onSecondaryContainer = TextOnDark,
    error = EmergencyRed,
    onError = TextOnDark,
    outline = MutedSlate
)

@Composable
fun EmergencySupportTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EmergencyColors,
        typography = AppTypography,
        content = content
    )
}
