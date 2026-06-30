package com.maximeze.browser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Maximeze brand palette — vibrant violet/purple
val MaximizeViolet = Color(0xFF7C3AED)
val MaximizeVioletLight = Color(0xFFA78BFA)
val MaximizeCyan = Color(0xFF06B6D4)
val MaximizePink = Color(0xFFEC4899)

private val LightColorScheme = lightColorScheme(
    primary = MaximizeViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF3B0764),
    secondary = MaximizeCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = MaximizePink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF500724),
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1A1025),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1025),
    surfaceVariant = Color(0xFFF3EEFF),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFD0C4F7),
    outlineVariant = Color(0xFFCAC4D0),
    inverseSurface = Color(0xFF2D2640),
    inverseOnSurface = Color(0xFFF2EEFF),
    inversePrimary = MaximizeVioletLight,
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = MaximizeVioletLight,
    onPrimary = Color(0xFF3B0764),
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF083344),
    secondaryContainer = Color(0xFF0E7490),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF500724),
    tertiaryContainer = Color(0xFF9D174D),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF0D0914),   // deep dark purple-black
    onBackground = Color(0xFFEDE9FE),
    surface = Color(0xFF17112A),      // dark purple surface
    onSurface = Color(0xFFE8E0F5),
    surfaceVariant = Color(0xFF261C3D),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF3D2F5A),
    outlineVariant = Color(0xFF4A3D6A),
    inverseSurface = Color(0xFFEDE9FE),
    inverseOnSurface = Color(0xFF2D2640),
    inversePrimary = MaximizeViolet,
    scrim = Color(0xFF000000),
)

@Composable
fun MaximizeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaximizeTypography,
        content = content,
    )
}
