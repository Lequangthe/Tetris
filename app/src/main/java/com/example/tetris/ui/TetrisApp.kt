package com.example.tetris.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tetris.logic.TetrisViewModel

@Composable
fun TetrisApp(viewModel: TetrisViewModel) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel.showSettings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.showSettings = false }
            )
        } else {
            TetrisScreen(
                viewModel = viewModel,
                onSettingsClick = { viewModel.showSettings = true }
            )
        }
    }
}