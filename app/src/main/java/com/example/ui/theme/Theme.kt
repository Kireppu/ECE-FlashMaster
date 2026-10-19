package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimaryDark,
    onPrimary = Color(0xFF2E1000),
    primaryContainer = Color(0xFF4A1C00),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = BlazeSecondaryDark,
    onSecondary = Color(0xFF3B1500),
    secondaryContainer = Color(0xFF5A2200),
    onSecondaryContainer = Color(0xFFFFDCCE),
    tertiary = GoldTertiaryDark,
    onTertiary = Color(0xFF382A00),
    tertiaryContainer = Color(0xFF543E00),
    onTertiaryContainer = Color(0xFFFFDF94),
    background = ObsidianBlack,
    onBackground = Color(0xFFF4F4F5),
    surface = CharcoalSurface,
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = CharcoalBorder
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeLight,
    onPrimaryContainer = Color(0xFF361000),
    secondary = BlazeSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlazeLight,
    onSecondaryContainer = Color(0xFF381200),
    tertiary = GoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF261900),
    background = WarmPaperBackground,
    onBackground = Color(0xFF18181B),
    surface = LightSurfaceCard,
    onSurface = Color(0xFF18181B),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF52525B),
    outline = LightBorderColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to deep orange-black theme for sleek engineering HUD
    dynamicColor: Boolean = false, // Keep tailored orange-black gradient branding
    content: @Composable () -> Unit
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
        typography = Typography,
        content = content
    )
}
