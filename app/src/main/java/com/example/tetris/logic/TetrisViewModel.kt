package com.example.tetris.logic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tetris.audio.SoundManager
import com.example.tetris.audio.TetrisSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

class TetrisViewModel(application: Application) : AndroidViewModel(application) {

    // ========== GAME STATE (KHỞI TẠO ĐÚNG CÁCH) ==========
    var grid by mutableStateOf(Array(20) { Array(10) { 0L } })
        private set

    var currentPiece by mutableStateOf<TetrisPiece?>(null)
        private set

    var currentX by mutableStateOf(3)
        private set

    var currentY by mutableStateOf(0)
        private set

    var nextPiece by mutableStateOf<TetrisPiece?>(null)
        private set

    var score by mutableStateOf(0)
        private set

    var lines by mutableStateOf(0)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    var showSettings by mutableStateOf(false)

    // ========== SETTINGS STATE ==========
    var musicVolume by mutableStateOf(0.5f)
    var sfxVolume by mutableStateOf(0.5f)
    var isMusicOn by mutableStateOf(true)
    var isSfxOn by mutableStateOf(true)
    var isVibrationOn by mutableStateOf(true)
    var isGhostOn by mutableStateOf(true)
    var speedLevel by mutableStateOf(1)

    private val speedMap = mapOf(0 to 700L, 1 to 500L, 2 to 350L)
    private var gameJob: kotlinx.coroutines.Job? = null
    private var lockJob: kotlinx.coroutines.Job? = null
    private var prefs: SharedPreferences? = null

    private var soundManager: SoundManager? = null

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
        android.util.Log.d("TetrisVM", "ViewModel INITIALIZED")
        startGame()
    }

    fun initPrefs() {
        val context = getApplication<Application>()
        prefs = context.getSharedPreferences("tetris_settings", Context.MODE_PRIVATE)
        loadSettings()
        soundManager = SoundManager(context)
        soundManager?.startBGM()
    }

    private fun loadSettings() {
        musicVolume = prefs?.getFloat("music_volume", 0.5f) ?: 0.5f
        sfxVolume = prefs?.getFloat("sfx_volume", 0.5f) ?: 0.5f
        isMusicOn = prefs?.getBoolean("music_on", true) ?: true
        isSfxOn = prefs?.getBoolean("sfx_on", true) ?: true
        isVibrationOn = prefs?.getBoolean("vibration_on", true) ?: true
        isGhostOn = prefs?.getBoolean("ghost_on", true) ?: true
        speedLevel = prefs?.getInt("speed_level", 1) ?: 1
    }

    fun saveSettings() {
        prefs?.edit()?.apply {
            putFloat("music_volume", musicVolume)
            putFloat("sfx_volume", sfxVolume)
            putBoolean("music_on", isMusicOn)
            putBoolean("sfx_on", isSfxOn)
            putBoolean("vibration_on", isVibrationOn)
            putBoolean("ghost_on", isGhostOn)
            putInt("speed_level", speedLevel)
            apply()
        }
    }

    fun resetToDefaultSettings() {
        musicVolume = 0.5f
        sfxVolume = 0.5f
        isMusicOn = true
        isSfxOn = true
        isVibrationOn = true
        isGhostOn = true
        speedLevel = 1
        saveSettings()
    }

    fun changeSpeed(newLevel: Int) {
        speedLevel = newLevel
        saveSettings()
        if (!isPaused && !isGameOver) {
            gameJob?.cancel()
            startGameLoop()
        }
    }

    fun startGame() {
        resetGame()
    }

    fun resetGame() {
        android.util.Log.d("TetrisVM", "resetGame called")
        grid = Array(20) { Array(10) { 0L } }
        score = 0
        lines = 0
        isGameOver = false
        isPaused = false
        nextPiece = getRandomPiece()
        spawnNewPiece()
        gameJob?.cancel()
        startGameLoop()
    }

    private fun getRandomPiece(): TetrisPiece {
        return shapes.random()
    }

    private fun spawnNewPiece() {
        if (nextPiece == null) nextPiece = getRandomPiece()
        currentPiece = nextPiece!!
        nextPiece = getRandomPiece()
        currentX = 3
        currentY = 0
        if (collision(currentPiece!!.matrix, currentX, currentY)) {
            isGameOver = true
            gameJob?.cancel()
        }
    }

    private fun collision(matrix: Array<IntArray>, x: Int, y: Int): Boolean {
        for (row in matrix.indices) {
            for (col in matrix[0].indices) {
                if (matrix[row][col] != 0) {
                    val boardX = x + col
                    val boardY = y + row
                    if (boardX < 0 || boardX >= 10 || boardY >= 20 || boardY < 0) return true
                    if (boardY >= 0 && grid[boardY][boardX] != 0L) return true
                }
            }
        }
        return false
    }

    private fun mergePiece() {
        currentPiece?.let { piece ->
            val newGrid = grid.map { it.copyOf() }.toTypedArray()
            for (row in piece.matrix.indices) {
                for (col in piece.matrix[0].indices) {
                    if (piece.matrix[row][col] != 0) {
                        val x = currentX + col
                        val y = currentY + row
                        if (y in 0 until 20 && x in 0 until 10) {
                            newGrid[y][x] = piece.color
                        }
                    }
                }
            }
            grid = newGrid
            playSound("lock")
            clearLines()
            spawnNewPiece()
        }
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
                // Không tăng row, kiểm tra lại vị trí hiện tại
            } else {
                row--
            }
        }
        if (rowsCleared > 0) {
            grid = newGrid
            playSound("clear")
            val points = mapOf(1 to 40, 2 to 100, 3 to 300, 4 to 1200)
            score += points[rowsCleared] ?: 40
            lines += rowsCleared
        }
    }

    fun moveLeft() {
        if (isGameOver || isPaused) return
        currentPiece?.let {
            if (!collision(it.matrix, currentX - 1, currentY)) {
                currentX--
                playSound("move")
                resetLockDelay()
            }
        }
    }

    fun moveRight() {
        if (isGameOver || isPaused) return
        currentPiece?.let {
            if (!collision(it.matrix, currentX + 1, currentY)) {
                currentX++
                playSound("move")
                resetLockDelay()
            }
        }
    }

    fun rotatePiece() {
        if (isGameOver || isPaused) return
        val piece = currentPiece ?: return
        val rotated = Array(4) { IntArray(4) }
        for (i in 0..3) for (j in 0..3) rotated[j][3-i] = piece.matrix[i][j]

        val kicks = listOf(0 to 0, 1 to 0, -1 to 0, 2 to 0, -2 to 0)
        for ((dx, dy) in kicks) {
            if (!collision(rotated, currentX + dx, currentY + dy)) {
                currentPiece = piece.copy(matrix = rotated)
                currentX += dx
                currentY += dy
                playSound("rotate")
                resetLockDelay()
                return
            }
        }
        // All kicks failed — do nothing
    }

    private fun resetLockDelay() {
        lockJob?.cancel()
        currentPiece?.let {
            if (collision(it.matrix, currentX, currentY + 1)) {
                lockJob = viewModelScope.launch {
                    delay(500L)
                    mergePiece()
                }
            }
        }
    }

    fun hardDrop() {
        if (isGameOver || isPaused) return
        lockJob?.cancel()
        while (!collision(currentPiece!!.matrix, currentX, currentY + 1)) {
            currentY++
        }
        mergePiece()
    }

    fun movePieceDown() {
        if (isGameOver || isPaused) return
        currentPiece?.let {
            if (!collision(it.matrix, currentX, currentY + 1)) {
                currentY++
                lockJob?.cancel()
            } else {
                if (lockJob == null || !lockJob!!.isActive) {
                    resetLockDelay()
                }
            }
        }
    }

    fun togglePause() {
        if (!isGameOver) {
            isPaused = !isPaused
            if (!isPaused) {
                startGameLoop()
                soundManager?.resumeBGM()
            } else {
                gameJob?.cancel()
                soundManager?.pauseBGM()
            }
        }
    }

    fun onResume() {
        if (soundManager?.bgmEnabled == true) {
            soundManager?.resumeBGM()
        }
        if (!isGameOver && !isPaused) startGameLoop()
    }

    fun onPause() {
        soundManager?.pauseBGM()
        gameJob?.cancel()
    }

    fun onDestroy() {
        soundManager?.release()
    }

    fun setSoundManagerBGM(enabled: Boolean) {
        soundManager?.bgmEnabled = enabled
        if (!enabled) soundManager?.pauseBGM()
        else soundManager?.resumeBGM()
    }

    private fun startGameLoop() {
        android.util.Log.d("TetrisVM", "startGameLoop called, speed=${speedMap[speedLevel]}")
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!isGameOver && !isPaused) {
                delay(speedMap[speedLevel] ?: 500L)
                android.util.Log.d("TetrisVM", "Gravity tick")
                movePieceDown()
            }
        }
    }

    fun getGhostY(): Int {
        if (!isGhostOn || currentPiece == null) return currentY
        var ghostY = currentY
        while (!collision(currentPiece!!.matrix, currentX, ghostY + 1)) {
            ghostY++
        }
        return ghostY
    }

    fun playMusic() {
        soundManager?.resumeBGM()
    }

    fun stopMusic() {
        soundManager?.pauseBGM()
    }

    fun playSound(name: String) {
        if (!isSfxOn) return
        val sound = when(name) {
            "move"     -> TetrisSound.MOVE
            "rotate"   -> TetrisSound.ROTATE
            "lock"     -> TetrisSound.LOCK
            "clear"    -> TetrisSound.CLEAR
            "gameover" -> TetrisSound.GAME_OVER
            else       -> return
        }
        soundManager?.play(sound)
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        onDestroy()
    }
}