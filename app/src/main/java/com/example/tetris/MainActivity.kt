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
            val isDarkTheme = viewModel.themeMode == 1
            val view = LocalView.current
            
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = if (isDarkTheme) {
                        android.graphics.Color.BLACK
                    } else {
                        android.graphics.Color.parseColor("#F0F2F5")
                    }
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
                }
            }
            TETRISTheme(darkTheme = isDarkTheme) {
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
