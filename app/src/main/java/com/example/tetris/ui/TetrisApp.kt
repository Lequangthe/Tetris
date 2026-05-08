package com.example.tetris.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.tetris.logic.TetrisViewModel

@Composable
fun TetrisApp(viewModel: TetrisViewModel) {
    val context = LocalContext.current
    var lastBackTime by remember { mutableLongStateOf(0L) }

    // Xử lý nút Back hệ thống
    BackHandler {
        if (viewModel.showSettings) {
            // Nếu đang ở màn hình Settings, quay về màn hình chính
            viewModel.showSettings = false
        } else {
            // Nếu đang ở màn hình chính, yêu cầu nhấn 2 lần để thoát
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackTime < 2000) {
                // Thoát ứng dụng
                (context as? Activity)?.finish()
            } else {
                lastBackTime = currentTime
                Toast.makeText(context, "Nhấn back lần nữa để thoát", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                onSettingsClick = { viewModel.showSettings = true },
                isDark = viewModel.themeMode == 1
            )
        }
    }
}