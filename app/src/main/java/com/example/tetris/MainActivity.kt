package com.example.tetris

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.tetris.logic.TetrisViewModel
import com.example.tetris.ui.TetrisApp
import com.example.tetris.ui.theme.TETRISTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: TetrisViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        viewModel = TetrisViewModel()
        viewModel.initPrefs(applicationContext)
        
        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    window.statusBarColor = android.graphics.Color.parseColor("#F8F9FA")
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }
            TETRISTheme {
                TetrisApp(viewModel = viewModel)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        viewModel.stopMusic()
    }
    
    override fun onResume() {
        super.onResume()
        if (!viewModel.isGameOver && !viewModel.isPaused) {
            viewModel.playMusic()
        }
    }
}