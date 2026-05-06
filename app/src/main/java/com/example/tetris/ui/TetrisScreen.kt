package com.example.tetris.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.tetris.logic.TetrisPiece
import com.example.tetris.logic.TetrisViewModel
import kotlin.math.abs

@Composable
fun TetrisScreen(viewModel: TetrisViewModel, onSettingsClick: () -> Unit) {
    val grid          = viewModel.grid
    val currentPiece  = viewModel.currentPiece
    val currentX      = viewModel.currentX
    val currentY      = viewModel.currentY
    val ghostY        = viewModel.getGhostY()
    val nextPiece     = viewModel.nextPiece
    val score         = viewModel.score
    val lines         = viewModel.lines
    val speedLevel    = viewModel.speedLevel
    val isGameOver    = viewModel.isGameOver
    val isPaused      = viewModel.isPaused
    val isGhostOn     = viewModel.isGhostOn

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) { viewModel.onResume() }
            override fun onPause(owner: LifecycleOwner)  { viewModel.onPause()  }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    var dragStarted by remember { mutableStateOf(false) }
    var totalDx by remember { mutableStateOf(0f) }
    var totalDy by remember { mutableStateOf(0f) }
    var hasHardDropped by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatText("Score: $score")
                    StatText("Lines: $lines")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.togglePause() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color(0xFF455A64)
                        )
                    }
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF455A64))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Next Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                InfoSection(label = "Next:", piece = nextPiece)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.5f)
                    .background(Color(0xFFF0F0F0))
                    .border(1.5.dp, Color(0xFF78909C))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (!dragStarted) viewModel.rotatePiece()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                totalDx = 0f
                                totalDy = 0f
                                hasHardDropped = false
                                dragStarted = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDx += dragAmount.x
                                totalDy += dragAmount.y

                                // Hard drop check
                                if (totalDy > 160f && abs(totalDy) > abs(totalDx) * 3f) {
                                    if (!hasHardDropped) {
                                        viewModel.hardDrop()
                                        hasHardDropped = true
                                    }
                                    return@detectDragGestures
                                }

                                // Horizontal move check
                                if (abs(totalDx) > 60f && abs(totalDx) > abs(totalDy) * 2f) {
                                    if (totalDx > 0) viewModel.moveRight() else viewModel.moveLeft()
                                    totalDx = 0f
                                }
                            },
                            onDragEnd = {
                                hasHardDropped = false
                                dragStarted = false
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / 10f
                    val cellH = size.height / 20f

                    // Grid cells
                    for (row in 0 until 20) {
                        for (col in 0 until 10) {
                            val colorVal = grid[row][col]
                            if (colorVal != 0L) {
                                drawRect(
                                    color = Color(colorVal),
                                    topLeft = Offset(col * cellW, row * cellH),
                                    size = Size(cellW, cellH)
                                )
                                drawRect(
                                    color = Color.White.copy(alpha = 0.25f),
                                    topLeft = Offset(col * cellW, row * cellH),
                                    size = Size(cellW, cellH * 0.3f)
                                )
                            }
                        }
                    }

                    // Ghost piece (Outline only)
                    if (isGhostOn && currentPiece != null) {
                        val piece = currentPiece
                        for (r in piece.matrix.indices) {
                            for (c in piece.matrix[0].indices) {
                                if (piece.matrix[r][c] != 0) {
                                    val gx = (currentX + c) * cellW
                                    val gy = (ghostY + r) * cellH
                                    drawRect(
                                        color = Color(piece.color).copy(alpha = 0.6f),
                                        topLeft = Offset(gx + 1f, gy + 1f),
                                        size = Size(cellW - 2f, cellH - 2f),
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    // Active piece
                    if (currentPiece != null) {
                        val piece = currentPiece
                        for (r in piece.matrix.indices) {
                            for (c in piece.matrix[0].indices) {
                                if (piece.matrix[r][c] != 0) {
                                    val px = (currentX + c) * cellW
                                    val py = (currentY + r) * cellH
                                    drawRect(
                                        color = Color(piece.color),
                                        topLeft = Offset(px, py),
                                        size = Size(cellW, cellH)
                                    )
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.35f),
                                        topLeft = Offset(px, py),
                                        size = Size(cellW, cellH * 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isGameOver) {
            GameResultOverlay(title = "GAME OVER", score = score, btnText = "RETRY") { viewModel.resetGame() }
        }
        if (isPaused && !isGameOver) {
            GameResultOverlay(title = "PAUSED", score = score, btnText = "RESUME") { viewModel.togglePause() }
        }
    }
}

@Composable
private fun StatText(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF263238)
    )
}

@Composable
private fun InfoSection(label: String, piece: TetrisPiece?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White)
                .border(1.dp, Color(0xFFB0BEC5)),
            contentAlignment = Alignment.Center
        ) {
            if (piece != null) {
                Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                    val cellSize = minOf(size.width / 4f, size.height / 4f)
                    val ox = (size.width - piece.matrix[0].size * cellSize) / 2f
                    val oy = (size.height - piece.matrix.size * cellSize) / 2f
                    for (r in piece.matrix.indices) {
                        for (c in piece.matrix[0].indices) {
                            if (piece.matrix[r][c] != 0) {
                                drawRect(
                                    color = Color(piece.color),
                                    topLeft = Offset(ox + c * cellSize, oy + r * cellSize),
                                    size = Size(cellSize - 1f, cellSize - 1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameResultOverlay(title: String, score: Int, btnText: String, onAction: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 48.dp)
            ) {
                Text(text = title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Score: $score", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color(0xFF455A64))
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text(text = btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}