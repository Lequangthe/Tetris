package com.example.tetris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.tetris.logic.Block
import com.example.tetris.logic.Piece
import com.example.tetris.logic.TetrominoType
import com.example.tetris.ui.theme.getTetrominoColor

@Composable
fun TetrisGrid(
    grid: List<List<TetrominoType?>>,
    currentPiece: Piece?,
    modifier: Modifier = Modifier,
    blockSize: androidx.compose.ui.unit.Dp = 30.dp
) {
    Box(
        modifier = modifier
            .size(width = blockSize * 10, height = blockSize * 20)
            .background(Color(0xFFF0F0F0)) // Light grey board background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pxSize = blockSize.toPx()
            
            // Draw grid lines
            for (i in 0..10) {
                drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(i * pxSize, 0f), Offset(i * pxSize, size.height), 0.5.dp.toPx())
            }
            for (i in 0..20) {
                drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(0f, i * pxSize), Offset(size.width, i * pxSize), 0.5.dp.toPx())
            }

            // Draw locked blocks
            grid.forEachIndexed { y, row ->
                row.forEachIndexed { x, type ->
                    if (type != null) {
                        drawRect(
                            color = getTetrominoColor(type),
                            topLeft = Offset(x * pxSize, y * pxSize),
                            size = Size(pxSize, pxSize)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.1f),
                            topLeft = Offset(x * pxSize, y * pxSize),
                            size = Size(pxSize, pxSize),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
            
            // Draw ghost piece (shadow)
            currentPiece?.let { piece ->
                var ghostY = piece.position.y
                while (isValidGhostPosition(piece.copy(position = Block(piece.position.x, ghostY + 1)), grid)) {
                    ghostY++
                }
                
                piece.blocks.forEach { block ->
                    val x = (block.x + piece.position.x) * pxSize
                    val y = (block.y + ghostY) * pxSize
                    if (y >= 0) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.05f),
                            topLeft = Offset(x, y),
                            size = Size(pxSize, pxSize),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
            
            // Draw current piece
            currentPiece?.absoluteBlocks?.forEach { block ->
                if (block.y >= 0) {
                    drawRect(
                        color = getTetrominoColor(currentPiece.type),
                        topLeft = Offset(block.x * pxSize, block.y * pxSize),
                        size = Size(pxSize, pxSize)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.1f),
                        topLeft = Offset(block.x * pxSize, block.y * pxSize),
                        size = Size(pxSize, pxSize),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun isValidGhostPosition(piece: Piece, grid: List<List<TetrominoType?>>): Boolean {
    return piece.absoluteBlocks.all { block ->
        block.x in 0..9 && block.y < 20 && (block.y < 0 || grid[block.y][block.x] == null)
    }
}

@Composable
fun NextPiecePreview(
    piece: Piece?,
    modifier: Modifier = Modifier,
    blockSize: androidx.compose.ui.unit.Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(blockSize * 4)
            .background(Color.White)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            Canvas(modifier = Modifier.size(blockSize * 3.5f)) {
                val pxSize = blockSize.toPx()
                val minX = piece.blocks.minOf { it.x }
                val maxX = piece.blocks.maxOf { it.x }
                val minY = piece.blocks.minOf { it.y }
                val maxY = piece.blocks.maxOf { it.y }
                
                val offsetX = (size.width - (maxX - minX + 1) * pxSize) / 2 - minX * pxSize
                val offsetY = (size.height - (maxY - minY + 1) * pxSize) / 2 - minY * pxSize

                piece.blocks.forEach { block ->
                    drawRect(
                        color = getTetrominoColor(piece.type),
                        topLeft = Offset(block.x * pxSize + offsetX, block.y * pxSize + offsetY),
                        size = Size(pxSize, pxSize)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.1f),
                        topLeft = Offset(block.x * pxSize + offsetX, block.y * pxSize + offsetY),
                        size = Size(pxSize, pxSize),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}
