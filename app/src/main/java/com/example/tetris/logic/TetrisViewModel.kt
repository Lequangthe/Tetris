package com.example.tetris.logic

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetris.audio.BonusFeedback
import com.example.tetris.audio.SoundManager
import com.example.tetris.audio.TetrisSound
import com.example.tetris.ui.effects.BonusEffectHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    // ========== GAME STATE ==========
    var grid by mutableStateOf(Array(20) { Array(10) { 0L } })
        private set

    private val _effectEvent = MutableSharedFlow<EffectType>(extraBufferCapacity = 10)
    val effectEvent = _effectEvent.asSharedFlow()

    var score by mutableStateOf(0)
        private set

    var lines by mutableStateOf(0)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    // ========== SETTINGS & UI STATE ==========
    var themeMode by mutableStateOf(0)
    var showSettings by mutableStateOf(false)
    var musicVolume by mutableStateOf(0.5f)
    var sfxVolume by mutableStateOf(0.5f)
    var isMusicOn by mutableStateOf(true)
    var isGhostOn by mutableStateOf(true)
    var speedLevel by mutableStateOf(1)
    var isPaused by mutableStateOf(false)
    var isAutoFall by mutableStateOf(false) // Mặc định tắt để bạn trải nghiệm kiểu "hiện đại"

    var currentPiece by mutableStateOf<TetrisPiece?>(null)
    var nextPiece by mutableStateOf<TetrisPiece?>(null)
    var currentX by mutableStateOf(3)
    var currentY by mutableStateOf(0)

    var powerUpMessage by mutableStateOf<String?>(null)
    var powerUpCooldownEnd by mutableStateOf(0L)

    var isSfxOn by mutableStateOf(true)
    var isVibrationOn by mutableStateOf(true)

    private var soundManager: SoundManager? = null
    private var currentActivity: Activity? = null

    private val shapes = listOf(
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(1,1,1,1), intArrayOf(0,0,0,0), intArrayOf(0,0,0,0)), 0xFF00E5F0, "I"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,1,1,0), intArrayOf(0,1,1,0), intArrayOf(0,0,0,0)), 0xFFF7E476, "O"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,1,0,0), intArrayOf(1,1,1,0), intArrayOf(0,0,0,0)), 0xFFCF6FDD, "T"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,1,1,0), intArrayOf(1,1,0,0), intArrayOf(0,0,0,0)), 0xFF6FCF97, "S"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(1,1,0,0), intArrayOf(0,1,1,0), intArrayOf(0,0,0,0)), 0xFFE66767, "Z"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(1,0,0,0), intArrayOf(1,1,1,0), intArrayOf(0,0,0,0)), 0xFFF2A65A, "L"),
        TetrisPiece(arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,0,1,0), intArrayOf(1,1,1,0), intArrayOf(0,0,0,0)), 0xFF5A8DF2, "J")
    )

    init {
        resetGame()
    }

    fun initPrefs() {
        val prefs = getApplication<Application>().getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)
        themeMode = prefs.getInt("themeMode", 0)
        musicVolume = prefs.getFloat("musicVolume", 0.5f)
        sfxVolume = prefs.getFloat("sfxVolume", 0.5f)
        isMusicOn = prefs.getBoolean("isMusicOn", true)
        isSfxOn = prefs.getBoolean("isSfxOn", true)
        isVibrationOn = prefs.getBoolean("isVibrationOn", true)
        isGhostOn = prefs.getBoolean("isGhostOn", true)
        speedLevel = prefs.getInt("speedLevel", 1)
        // Load saved game progress
        score = prefs.getInt("score", 0)
        lines = prefs.getInt("lines", 0)
        // Load saved grid and piece state
        val gridStr = prefs.getString("grid", null)
        if (gridStr != null) {
            decodeGrid(gridStr)
        }
        currentX = prefs.getInt("currentX", 3)
        currentY = prefs.getInt("currentY", 0)
        val curName = prefs.getString("currentPiece", null)
        if (curName != null) {
            currentPiece = shapes.find { it.name == curName }
        }
        val nextName = prefs.getString("nextPiece", null)
        if (nextName != null) {
            nextPiece = shapes.find { it.name == nextName }
        }
        isGameOver = prefs.getBoolean("isGameOver", false)
    }

    private fun ensureManagersInitialized() {
        if (soundManager == null) {
            soundManager = SoundManager(getApplication())
            soundManager?.bgmEnabled = isMusicOn
            soundManager?.startBGM()
        }
    }

    fun saveSettings() {
        val prefs = getApplication<Application>().getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("themeMode", themeMode)
            putFloat("musicVolume", musicVolume)
            putFloat("sfxVolume", sfxVolume)
            putBoolean("isMusicOn", isMusicOn)
            putBoolean("isSfxOn", isSfxOn)
            putBoolean("isVibrationOn", isVibrationOn)
            putBoolean("isGhostOn", isGhostOn)
            putInt("speedLevel", speedLevel)
            // Save current game progress
            putInt("score", score)
            putInt("lines", lines)
            // Save grid and piece state
            putString("grid", encodeGrid())
            putInt("currentX", currentX)
            putInt("currentY", currentY)
            putString("currentPiece", currentPiece?.name)
            putString("nextPiece", nextPiece?.name)
            putBoolean("isGameOver", isGameOver)
            apply()
        }
    }

    fun resetToDefaultSettings() {
        themeMode = 0
        musicVolume = 0.5f
        sfxVolume = 0.5f
        isMusicOn = true
        isSfxOn = true
        isVibrationOn = true
        isGhostOn = true
        speedLevel = 1
        saveSettings()
    }

    fun onResume() {
        soundManager?.resumeBGM()
        isPaused = false
    }

    fun onPause() {
        soundManager?.pauseBGM()
        isPaused = true
        // Save progress when pausing or exiting
        saveSettings() // this now also saves score and lines
    }

    fun setSoundManagerBGM(enabled: Boolean) {
        soundManager?.bgmEnabled = enabled
    }

    fun changeSpeed(level: Int) {
        speedLevel = level
        saveSettings()
    }

    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) soundManager?.pauseBGM() else soundManager?.resumeBGM()
    }

    fun toggleAutoFall() {
        isAutoFall = !isAutoFall
    }

    private var gameJob: kotlinx.coroutines.Job? = null
    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (true) {
                val delayTime = when (speedLevel) {
                    0 -> 1000L
                    1 -> 600L
                    2 -> 300L
                    else -> 600L
                }
                delay(delayTime)
                if (isAutoFall && !isPaused && !isGameOver) {
                    moveDown()
                }
            }
        }
    }

    fun init(activity: Activity) {
        currentActivity = activity
        ensureManagersInitialized()
        startGameLoop()
    }

    fun resetGame() {
        grid = Array(20) { Array(10) { 0L } }
        score = 0
        lines = 0
        isGameOver = false
        isPaused = false
        spawnPiece()
    }

    private fun spawnPiece() {
        val newPiece = nextPiece ?: shapes.random()
        currentPiece = newPiece
        nextPiece = shapes.random()
        currentX = 3
        currentY = 0
        
        if (checkCollision(currentX, currentY, newPiece)) {
            isGameOver = true
            // Reset score and lines on game over
            score = 0
            lines = 0
            saveSettings()
            if (isSfxOn) soundManager?.play(TetrisSound.GAME_OVER)
        }
    }

    private fun checkCollision(x: Int, y: Int, piece: TetrisPiece): Boolean {
        for (r in piece.matrix.indices) {
            for (c in piece.matrix[0].indices) {
                if (piece.matrix[r][c] != 0) {
                    val newX = x + c
                    val newY = y + r
                    if (newX !in 0 until 10 || newY >= 20 || (newY >= 0 && grid[newY][newX] != 0L)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun moveLeft() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        if (!checkCollision(currentX - 1, currentY, piece)) {
            currentX--
            if (isSfxOn) soundManager?.play(TetrisSound.MOVE)
        }
    }

    fun moveRight() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        if (!checkCollision(currentX + 1, currentY, piece)) {
            currentX++
            if (isSfxOn) soundManager?.play(TetrisSound.MOVE)
        }
    }

    fun rotatePiece() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        val matrix = piece.matrix
        val n = matrix.size
        val rotated = Array(n) { IntArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                rotated[j][n - 1 - i] = matrix[i][j]
            }
        }
        val newPiece = piece.copy(matrix = rotated)
        
        // Wall-kick: Thử các vị trí lân cận để xoay nếu bị sát vách
        val offsets = listOf(0, -1, 1, -2, 2)
        for (offset in offsets) {
            if (!checkCollision(currentX + offset, currentY, newPiece)) {
                currentX += offset
                currentPiece = newPiece
                if (isSfxOn) soundManager?.play(TetrisSound.ROTATE)
                return
            }
        }
    }

    fun hardDrop() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        while (!checkCollision(currentX, currentY + 1, piece)) {
            currentY++
        }
        if (isSfxOn) soundManager?.play(TetrisSound.LOCK)
        lockPiece()
    }

    fun getGhostY(): Int {
        val piece = currentPiece ?: return 0
        var gy = currentY
        while (!checkCollision(currentX, gy + 1, piece)) {
            gy++
        }
        return gy
    }

    fun moveDown() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        if (!checkCollision(currentX, currentY + 1, piece)) {
            currentY++
        } else {
            lockPiece()
        }
    }

    private fun lockPiece() {
        val piece = currentPiece ?: return
        val newGrid = grid.map { it.copyOf() }.toTypedArray()
        for (r in piece.matrix.indices) {
            for (c in piece.matrix[0].indices) {
                if (piece.matrix[r][c] != 0) {
                    val gx = currentX + c
                    val gy = currentY + r
                    if (gy in 0 until 20 && gx in 0 until 10) {
                        newGrid[gy][gx] = piece.color
                    }
                }
            }
        }
        grid = newGrid
        clearLines()
        spawnPiece()
    }

    fun activatePowerUp() {
        // Giữ lại khung nhưng có thể bỏ nội dung nếu bạn không dùng đến
    }

    private fun clearLines() {
        val newGrid = grid.map { it.copyOf() }.toTypedArray()
        var rowsCleared = 0
        var row = 19

        while (row >= 0) {
            if (newGrid[row].all { it != 0L }) {
                for (r in row downTo 1) {
                    newGrid[r] = newGrid[r - 1].copyOf()
                }
                newGrid[0] = Array(10) { 0L }
                rowsCleared++
            } else {
                row--
            }
        }

        if (rowsCleared > 0) {
            grid = newGrid
            val points = mapOf(1 to 40, 2 to 100, 3 to 300, 4 to 1200)
            score += points[rowsCleared] ?: 40
            lines += rowsCleared

            if (isSfxOn) soundManager?.play(TetrisSound.CLEAR)

            viewModelScope.launch {
                _effectEvent.emit(EffectType.LINE_CLEAR)
            }
        }
    }

    private fun encodeGrid(): String {
        return grid.joinToString(";") { row -> row.joinToString(",") }
    }

    private fun decodeGrid(gridStr: String) {
        try {
            val rows = gridStr.split(";")
            if (rows.size == 20) {
                val newGrid = Array(20) { r ->
                    val cols = rows[r].split(",")
                    Array(10) { c ->
                        cols[c].toLongOrNull() ?: 0L
                    }
                }
                grid = newGrid
            }
        } catch (_: Exception) {
            // Keep current grid if decoding fails
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Persist state on ViewModel clearance
        saveSettings()
        soundManager?.release()
    }
}

// ========== DATA CLASS ==========
data class TetrisPiece(
    val matrix: Array<IntArray>,
    val color: Long,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TetrisPiece) return false
        return name == other.name && color == other.color &&
                matrix.contentDeepEquals(other.matrix)
    }

    override fun hashCode(): Int {
        var result = matrix.contentDeepHashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}