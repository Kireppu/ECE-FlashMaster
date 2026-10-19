package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

// Soft Lighter Orange & Warm Apricot Amber Palette
val OrangePrimary = Color(0xFFFDBA74)
val OrangePrimaryDark = Color(0xFFFDBA74)
val OrangeLight = Color(0xFFFFEDD5)
val OrangeAccent = Color(0xFFFB923C)
val AmberGlow = Color(0xFFFBBF24)
val AmberRadiance = Color(0xFFF59E0B)

// Secondary Warm Blaze & Keycap Accents
val BlazeSecondary = Color(0xFFFB923C)
val BlazeSecondaryDark = Color(0xFFFDBA74)
val BlazeLight = Color(0xFFFFF7ED)

// Tertiary Precision Gold & Verification
val GoldTertiary = Color(0xFFFBBF24)
val GoldTertiaryDark = Color(0xFFFDE047)
val EmeraldSuccess = Color(0xFF22C55E)

// Dark Theme Surfaces: Obsidian Black & Translucent Charcoal
val ObsidianBlack = Color(0xFF09090C)
val CharcoalSurface = Color(0xFF121216)
val CharcoalElevated = Color(0xFF1A1A20)
val CharcoalBorder = Color(0xFF2A2A32)

// Low-Opacity Sheer Glassmorphism Tokens
val GlassyCardBackground = Color(0x4716161D)
val GlassyCardBackgroundElevated = Color(0x591C1C24)
val GlassyCardBorder = Color(0x28FDBA74)
val GlassyCardBorderHighlight = Color(0x44FDBA74)
val GlassyInputBackground = Color(0x551C1C22)

// Light Theme Surfaces
val WarmPaperBackground = Color(0xFFFAFAF9)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF5F5F4)
val LightBorderColor = Color(0xFFE7E5E4)

// Calculator Keycap Colors
val KeycapDark = Color(0xFF18181B)
val KeycapDarkBorder = Color(0xFF52525B)
val KeycapShiftOrange = Color(0xFFFDBA74)
val KeycapAlphaGold = Color(0xFFFBBF24)
val KeycapAppsAmber = Color(0xFFFB923C)
val KeycapModeRed = Color(0xFFF87171)
val KeycapText = Color(0xFFF4F4F5)
val LcdScreenBg = Color(0xFF121317)

// Softer, Lighter Warm Orange Gradient Brushes
val PrimaryOrangeGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFB923C),
        Color(0xFFF59E0B),
        Color(0xFFEA580C)
    )
)

val PrimaryOrangeDiagonalGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFDBA74),
        Color(0xFFFB923C),
        Color(0xFFF59E0B)
    )
)

val GlassyBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x4DFDBA74),
        Color(0x1CFB923C),
        Color(0x0AFFFFFF),
        Color(0x2CFDBA74)
    ),
    start = Offset(0f, 0f),
    end = Offset(500f, 500f)
)

val GlassySpecularHighlight = Brush.verticalGradient(
    colors = listOf(
        Color(0x10FFFFFF),
        Color(0x03FFFFFF),
        Color(0x00000000)
    )
)

val EmeraldMasteryGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF22C55E),
        Color(0xFF10B981)
    )
)

val SubtleOrangeGlow = Brush.radialGradient(
    colors = listOf(
        Color(0x28FB923C),
        Color(0x0DFB923C),
        Color(0x00000000)
    ),
    radius = 500f
)

// Brushes for conditional UI states
val RevealedButtonBrush = SolidColor(Color(0x24FB923C))
val DisabledButtonBrush = SolidColor(Color(0x2852525B))

// Semantic token aliases mapped to the Soft Orange-Black theme
val EmeraldTertiary = EmeraldSuccess
val AmberSecondaryDark = AmberGlow
val CyanPrimary = OrangePrimary
val KeycapAlphaOrange = KeycapAlphaGold
val KeycapAppsTeal = KeycapAppsAmber
val KeycapShiftGold = KeycapShiftOrange
