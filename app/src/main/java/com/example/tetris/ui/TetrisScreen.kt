package com.example.tetris.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.res.painterResource
import com.example.tetris.logic.TetrisViewModel
import com.example.tetris.logic.EffectType
import com.example.tetris.ui.theme.LocalTetrisColors
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Animatable<Float, AnimationVector1D>,
    val y: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>,
    val color: Color,
    val size: Float
)

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
    val isAutoFall    = viewModel.isAutoFall

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

    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var showFlash by remember { mutableStateOf(false) }
    val flashAlpha = remember { Animatable(0f) }

    val coroutineScope = rememberCoroutineScope()

    fun spawnParticles(centerX: Float, centerY: Float) {
        val newParticles = List(30) {
            val color = listOf(Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Cyan, Color(0xFFA020F0)).random()
            val size = Random.nextFloat() * (18f - 6f) + 6f
            val angle = Random.nextFloat() * 360f
            val distance = Random.nextFloat() * (300f - 80f) + 80f
            val targetX = centerX + (distance * cos(Math.toRadians(angle.toDouble()))).toFloat()
            val targetY = centerY + (distance * sin(Math.toRadians(angle.toDouble()))).toFloat()

            Particle(
                x = Animatable(centerX),
                y = Animatable(centerY),
                alpha = Animatable(1f),
                color = color,
                size = size
            ).also { p ->
                coroutineScope.launch {
                    launch { p.x.animateTo(targetX, tween(600)) }
                    launch { p.y.animateTo(targetY, tween(600)) }
                    launch { p.alpha.animateTo(0f, tween(500)) }
                }
            }
        }
        particles = particles + newParticles
        coroutineScope.launch {
            delay(700)
            particles = particles.filter { it.alpha.value > 0f }
        }
    }

    LaunchedEffect(showFlash) {
        if (showFlash) {
            flashAlpha.snapTo(0.85f)
            flashAlpha.animateTo(0f, tween(120))
            showFlash = false
        }
    }

    var boardCenterX by remember { mutableStateOf(0f) }
    var boardCenterY by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        viewModel.effectEvent.collect { effect ->
            when (effect) {
                EffectType.LINE_CLEAR -> {
                    spawnParticles(boardCenterX, boardCenterY)
                }
                else -> {}
            }
        }
    }

    var easterEggClicks by remember { mutableStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }

    var linesClickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val powerUpMessage = viewModel.powerUpMessage

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(key1 = viewModel.powerUpCooldownEnd) {
        while (viewModel.powerUpCooldownEnd > System.currentTimeMillis()) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }
    val cooldownRemaining = if (viewModel.powerUpCooldownEnd > currentTime) {
        val r = viewModel.powerUpCooldownEnd - currentTime
        "${r / 60000}:${String.format(Locale.ROOT, "%02d", (r % 60000) / 1000)}"
    } else null

    val colors = LocalTetrisColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.screenBg)
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
                    StatItem(label = "SCORE", value = "$score", valueColor = colors.accentCyan, colors = colors)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    val now = System.currentTimeMillis()
                                    if (now - lastClickTime < 500) {
                                        linesClickCount = 0
                                        viewModel.activatePowerUp()
                                    } else {
                                        linesClickCount++
                                    }
                                    lastClickTime = now
                                })
                            }
                        ) {
                            StatItem(label = "LINES", value = "$lines", valueColor = colors.accentAmber, colors = colors)
                        }
                        cooldownRemaining?.let {
                            Text(text = it, fontSize = 10.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.toggleAutoFall() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Toggle Gravity",
                            tint = if (isAutoFall) colors.accentCyan else colors.textMuted
                        )
                    }
                    IconButton(onClick = { viewModel.togglePause() }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = colors.textPrimary
                        )
                    }
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colors.textPrimary)
                    }
                }
            }

            powerUpMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.boardBg,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Next Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                InfoSection(
                    label = "Next:",
                    piece = nextPiece,
                    colors = colors,
                    onClick = {
                        easterEggClicks++
                        if (easterEggClicks >= 4) {
                            showEasterEgg = true
                            easterEggClicks = 0
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Board
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.5f)
            ) {
                boardCenterX = constraints.maxWidth.toFloat() / 2f
                boardCenterY = constraints.maxHeight.toFloat() / 2f

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x18000000))
                        .background(colors.boardBg, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.panelBorder, RoundedCornerShape(12.dp))
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

                        // Grid lines/Empty cells
                        for (row in 0 until 20) {
                            for (col in 0 until 10) {
                                drawRect(
                                    color = colors.cellEmpty,
                                    topLeft = Offset(col * cellW, row * cellH),
                                    size = Size(cellW, cellH)
                                )
                                drawRect(
                                    color = colors.cellGrid,
                                    topLeft = Offset(col * cellW, row * cellH),
                                    size = Size(cellW, cellH),
                                    style = Stroke(width = 0.5.dp.toPx())
                                )
                            }
                        }

                        // Grid cells with blocks
                        for (row in 0 until 20) {
                            for (col in 0 until 10) {
                                val colorVal = grid[row][col]
                                if (colorVal != 0L) {
                                    val blockColor = Color(colorVal)
                                    val x = col * cellW
                                    val y = row * cellH

                                    // Layer 1: Main fill
                                    drawRect(
                                        color = blockColor,
                                        topLeft = Offset(x + 1f, y + 1f),
                                        size = Size(cellW - 2f, cellH - 2f)
                                    )
                                    
                                    // Layer 2: Top highlight
                                    drawRect(
                                        color = Color.White.copy(alpha = 0.30f),
                                        topLeft = Offset(x + 1f, y + 1f),
                                        size = Size(cellW - 2f, cellH * 0.28f)
                                    )
                                    
                                    // Layer 3: Bottom shadow
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.25f),
                                        topLeft = Offset(x + 1f, y + cellH * 0.72f),
                                        size = Size(cellW - 2f, cellH * 0.28f)
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
                                        val blockColor = Color(piece.color)

                                        // Layer 1: Main fill
                                        drawRect(
                                            color = blockColor,
                                            topLeft = Offset(px + 1f, py + 1f),
                                            size = Size(cellW - 2f, cellH - 2f)
                                        )
                                        
                                        // Layer 2: Top highlight
                                        drawRect(
                                            color = Color.White.copy(alpha = 0.30f),
                                            topLeft = Offset(px + 1f, py + 1f),
                                            size = Size(cellW - 2f, cellH * 0.28f)
                                        )
                                        
                                        // Layer 3: Bottom shadow
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.25f),
                                            topLeft = Offset(px + 1f, py + cellH * 0.72f),
                                            size = Size(cellW - 2f, cellH * 0.28f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Draw particles ABOVE the board
                particles.forEach { p ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha.value),
                            radius = p.size,
                            center = Offset(p.x.value, p.y.value)
                        )
                    }
                }
            }
        }

        if (isGameOver) {
            GameResultOverlay(title = "GAME OVER", score = score, btnText = "RETRY", colors = colors) { viewModel.resetGame() }
        }
        if (isPaused && !isGameOver) {
            GameResultOverlay(title = "PAUSED", score = score, btnText = "RESUME", colors = colors) { viewModel.togglePause() }
        }

        if (showEasterEgg) {
            AlertDialog(
                onDismissRequest = { showEasterEgg = false },
                confirmButton = {
                    TextButton(onClick = { showEasterEgg = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Thông tin tác giả") },
                text = {
                    Column {
                        Text("Tác giả: Lê Quang Thế")
                        Text("Zalo: 0387220880")
                    }
                }
            )
        }

        if (flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha.value))
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color, colors: com.example.tetris.ui.theme.TetrisColors) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textMuted
        )
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}

@Composable
private fun InfoSection(label: String, piece: TetrisPiece?, colors: com.example.tetris.ui.theme.TetrisColors, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .background(colors.panelBg, RoundedCornerShape(12.dp))
                .border(1.dp, colors.panelBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (piece != null) {
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val cellSize = minOf(size.width / 4f, size.height / 4f)
                    val ox = (size.width - piece.matrix[0].size * cellSize) / 2f
                    val oy = (size.height - piece.matrix.size * cellSize) / 2f
                    for (r in piece.matrix.indices) {
                        for (c in piece.matrix[0].indices) {
                            if (piece.matrix[r][c] != 0) {
                                val blockColor = Color(piece.color)
                                val x = ox + c * cellSize
                                val y = oy + r * cellSize
                                
                                // Layer 1: Main fill
                                drawRect(
                                    color = blockColor,
                                    topLeft = Offset(x + 0.5f, y + 0.5f),
                                    size = Size(cellSize - 1f, cellSize - 1f)
                                )
                                
                                // Layer 2: Top highlight
                                drawRect(
                                    color = Color.White.copy(alpha = 0.30f),
                                    topLeft = Offset(x + 0.5f, y + 0.5f),
                                    size = Size(cellSize - 1f, cellSize * 0.28f)
                                )
                                
                                // Layer 3: Bottom shadow
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    topLeft = Offset(x + 0.5f, y + cellSize * 0.72f),
                                    size = Size(cellSize - 1f, cellSize * 0.28f)
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
private fun GameResultOverlay(title: String, score: Int, btnText: String, colors: com.example.tetris.ui.theme.TetrisColors, onAction: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.panelBg,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 40.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FINAL SCORE",
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = colors.textMuted
                )
                Text(
                    text = "$score",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentCyan
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.textPrimary,
                        contentColor = colors.screenBg
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
