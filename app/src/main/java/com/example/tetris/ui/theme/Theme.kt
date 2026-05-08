package com.example.tetris.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TetrisColors(
    val screenBg: Color,
    val boardBg: Color,
    val cellEmpty: Color,
    val cellGrid: Color,
    val panelBg: Color,
    val panelBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accentCyan: Color,
    val accentAmber: Color
)

val LightTokens = TetrisColors(
    screenBg = Color(0xFFF0F2F5),
    boardBg = Color(0xFFE8EAF0),
    cellEmpty = Color(0xFFDDE1EA),
    cellGrid = Color(0xFFC8CBD8),
    panelBg = Color(0xFFFFFFFF),
    panelBorder = Color(0xFFC8CBD8),
    textPrimary = Color(0xFF1A1F36),
    textMuted = Color(0xFF8A94A6),
    accentCyan = Color(0xFF00BCD4),
    accentAmber = Color(0xFFFFB300)
)

val DarkTokens = TetrisColors(
    screenBg = Color(0xFF000000),
    boardBg = Color(0xFF0D0F1A),
    cellEmpty = Color(0xFF111420),
    cellGrid = Color(0xFF1A1D2E),
    panelBg = Color(0xFF111111),
    panelBorder = Color(0xFF1A1D2E),
    textPrimary = Color(0xFFE8EAFF),
    textMuted = Color(0xFF555570),
    accentCyan = Color(0xFF00E5FF),
    accentAmber = Color(0xFFFFB300)
)

val LocalTetrisColors = staticCompositionLocalOf { LightTokens }

@Composable
fun TETRISTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkTokens else LightTokens
    
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = colors.screenBg,
            surface = colors.panelBg,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    } else {
        lightColorScheme(
            background = colors.screenBg,
            surface = colors.panelBg,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    }

    CompositionLocalProvider(LocalTetrisColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
