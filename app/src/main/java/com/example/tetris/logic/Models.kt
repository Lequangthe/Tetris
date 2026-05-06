package com.example.tetris.logic

import androidx.compose.runtime.Immutable

enum class TetrominoType {
    I, J, L, O, S, T, Z
}

data class Block(val x: Int, val y: Int)

data class Piece(
    val type: TetrominoType,
    val blocks: List<Block>,
    val position: Block = Block(3, 0)
) {
    fun rotated(): Piece {
        if (type == TetrominoType.O) return this
        val rotatedBlocks = blocks.map { block ->
            val relX = block.x - 1
            val relY = block.y - 1
            Block(1 - relY, 1 + relX)
        }
        return copy(blocks = rotatedBlocks)
    }

    fun move(dx: Int, dy: Int): Piece {
        return copy(position = Block(position.x + dx, position.y + dy))
    }

    val absoluteBlocks: List<Block>
        get() = blocks.map { Block(it.x + position.x, it.y + position.y) }
}

fun createPiece(type: TetrominoType): Piece {
    val blocks = when (type) {
        TetrominoType.I -> listOf(Block(0, 1), Block(1, 1), Block(2, 1), Block(3, 1))
        TetrominoType.J -> listOf(Block(0, 0), Block(0, 1), Block(1, 1), Block(2, 1))
        TetrominoType.L -> listOf(Block(2, 0), Block(0, 1), Block(1, 1), Block(2, 1))
        TetrominoType.O -> listOf(Block(1, 0), Block(2, 0), Block(1, 1), Block(2, 1))
        TetrominoType.S -> listOf(Block(1, 0), Block(2, 0), Block(0, 1), Block(1, 1))
        TetrominoType.T -> listOf(Block(1, 0), Block(0, 1), Block(1, 1), Block(2, 1))
        TetrominoType.Z -> listOf(Block(0, 0), Block(1, 0), Block(1, 1), Block(2, 1))
    }
    return Piece(type, blocks)
}

@Immutable
data class TetrisState(
    val grid: List<List<TetrominoType?>> = List(20) { List(10) { null } },
    val currentPiece: Piece? = null,
    val nextPiece: Piece? = null,
    val heldPiece: Piece? = null,
    val canHold: Boolean = true,
    val score: Int = 0,
    val linesCleared: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false
)
