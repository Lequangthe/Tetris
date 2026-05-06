package com.example.tetris.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TETRISTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Color(0xFFF8F9FA),
            surface    = Color.White,
            primary    = Color(0xFF4A6FA5)
        ),
        content = content
    )
}