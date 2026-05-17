package com.example.tetris

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tetris.logic.TetrisViewModel
import com.example.tetris.ui.TetrisApp
import com.example.tetris.ui.theme.TETRISTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TetrisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.initPrefs()
        viewModel.init(this)

        setContent {
            val isDarkTheme = viewModel.themeMode != 0
            val themeMode = viewModel.themeMode
            val view = LocalView.current
            
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = when(themeMode) {
                        0 -> android.graphics.Color.parseColor("#F0F2F5")
                        1 -> android.graphics.Color.BLACK
                        2 -> android.graphics.Color.parseColor("#0B001F")
                        else -> android.graphics.Color.BLACK
                    }
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                }
            }
            TETRISTheme(themeMode = themeMode) {
                TetrisApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }
}
