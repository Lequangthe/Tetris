package com.example.tetris.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.tetris.logic.TetrominoType

// Pastel Tetris color palette
val ColorI = Color(0xFFAEC6CF) // Pastel Blue
val ColorJ = Color(0xFFFFB347) // Pastel Orange
val ColorL = Color(0xFFB39EB5) // Pastel Purple
val ColorO = Color(0xFF0557FF) // Pastel Yellow
val ColorS = Color(0xFF00FF00) // Pastel Green
val ColorT = Color(0xFFC700FF) // Pastel Red
val ColorZ = Color(0xFFF1DA06) // Pastel Violet

fun getTetrominoColor(type: TetrominoType?): Color {
    return when (type) {
        TetrominoType.I -> ColorI
        TetrominoType.J -> ColorJ
        TetrominoType.L -> ColorL
        TetrominoType.O -> ColorO
        TetrominoType.S -> ColorS
        TetrominoType.T -> ColorT
        TetrominoType.Z -> ColorZ
        null -> Color.Transparent
    }
}

val DarkPrimary = Color(0xFF333333)
val DarkSecondary = Color(0xFF555555)
val DarkTertiary = Color(0xFF777777)

val LightPrimary = Color(0xFF000000)
val LightSecondary = Color(0xFF333333)
val LightTertiary = Color(0xFF666666)
