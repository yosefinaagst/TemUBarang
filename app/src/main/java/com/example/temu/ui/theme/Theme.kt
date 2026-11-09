
package com.example.temu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Maroon,
    onPrimary = TextWhite,

    secondary = MaroonDark,
    onSecondary = TextWhite,

    tertiary = DustyPink,
    onTertiary = TextPrimary,

    background = Cream,
    onBackground = TextPrimary,

    surface = WarmWhite,
    onSurface = TextPrimary,

    surfaceVariant = SoftPink,
    onSurfaceVariant = TextSecondary,

    outline = BorderColor,

    error = ErrorColor,
    onError = TextWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DustyPink,
    onPrimary = TextPrimary,

    secondary = Maroon,
    onSecondary = TextWhite,

    tertiary = SoftPink,
    onTertiary = TextPrimary,

    background = Color(0xFF211B1D),
    onBackground = TextWhite,

    surface = Color(0xFF30272A),
    onSurface = TextWhite,

    surfaceVariant = Color(0xFF49383E),
    onSurfaceVariant = DustyPink,

    outline = Color(0xFF705B62),

    error = ErrorColor,
    onError = TextWhite
)

@Composable
fun TemUTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
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
