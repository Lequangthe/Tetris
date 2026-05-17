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

// Bảng màu Tetromino tương ứng với từng theme
data class TetrominoPalette(
    val I: Long,
    val O: Long,
    val T: Long,
    val S: Long,
    val Z: Long,
    val L: Long,
    val J: Long
)

val ClassicPalette = TetrominoPalette(
    I = 0xFF00FFFF, // Cyan
    O = 0xFFFFFF00, // Yellow
    T = 0xFF800080, // Purple
    S = 0xFF00FF00, // Green
    Z = 0xFFFF0000, // Red
    L = 0xFFFFA500, // Orange
    J = 0xFF0000FF  // Blue
)

val NeonPalette = TetrominoPalette(
    I = 0xFF00E5F0,
    O = 0xFFF7E476,
    T = 0xFFCF6FDD,
    S = 0xFF6FCF97,
    Z = 0xFFE66767,
    L = 0xFFF2A65A,
    J = 0xFF5A8DF2
)

val MinimalistPalette = TetrominoPalette(
    I = 0xFF4A4A4A,
    O = 0xFF5E5E5E,
    T = 0xFF727272,
    S = 0xFF868686,
    Z = 0xFF9A9A9A,
    L = 0xFFAEAEAE,
    J = 0xFFC2C2C2
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

val CyberpunkTokens = TetrisColors(
    screenBg = Color(0xFF0B001F),
    boardBg = Color(0xFF1B003F),
    cellEmpty = Color(0xFF2B005F),
    cellGrid = Color(0xFF3B007F),
    panelBg = Color(0xFF1B003F),
    panelBorder = Color(0xFFFA00FF),
    textPrimary = Color(0xFFFA00FF),
    textMuted = Color(0xFF00E5FF),
    accentCyan = Color(0xFF00E5FF),
    accentAmber = Color(0xFFFA00FF)
)

val LocalTetrisColors = staticCompositionLocalOf { LightTokens }

@Composable
fun TETRISTheme(
    themeMode: Int = 0, // 0: Light, 1: Dark, 2: Cyberpunk
    content: @Composable () -> Unit
) {
    val colors = when(themeMode) {
        0 -> LightTokens
        1 -> DarkTokens
        2 -> CyberpunkTokens
        else -> DarkTokens
    }
    
    val darkTheme = themeMode != 0
    
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
