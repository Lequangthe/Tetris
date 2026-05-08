package com.example.tetris.logic

import android.content.Context
import com.example.tetris.R
import kotlin.random.Random

enum class BonusEffect {
    EXPLOSION, LIGHTNING_STRIKE, MONO_CHANGE, COLOR_PURGE, GHOST_SWAP
}

enum class BonusType(
    val displayName: String,
    val iconRes: Int,
    val bonusScore: Int,
    val extraLines: Int,
    val effect: BonusEffect
) {
    MONO_FILL("Lấp đầy màu", android.R.drawable.ic_menu_edit, 500, 2, BonusEffect.MONO_CHANGE),
    BOMB("Bom nổ", android.R.drawable.ic_delete, 300, 1, BonusEffect.EXPLOSION),
    LIGHTNING("Tia sét", android.R.drawable.ic_menu_compass, 400, 1, BonusEffect.LIGHTNING_STRIKE),
    COLOR_BOMB("Bom màu", android.R.drawable.ic_menu_gallery, 600, 3, BonusEffect.COLOR_PURGE),
    GHOST("Ma thuật", android.R.drawable.ic_menu_view, 200, 0, BonusEffect.GHOST_SWAP)
}

class BonusManager {

    /**
     * Kích hoạt bonus và thực hiện thuật toán thay đổi board.
     * Trả về số điểm thưởng nhận được.
     */
    fun activateBonus(
        type: BonusType,
        board: Array<Array<Long>>,
        onEffectComplete: () -> Unit
    ): Int {
        val rows = board.size
        val cols = board[0].size

        when (type) {
            BonusType.MONO_FILL -> {
                // Biến toàn bộ board thành một màu duy nhất (ví dụ: màu xanh lơ 0xFF00E5F0)
                val monoColor = 0xFF00E5F0
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        if (board[i][j] != 0L) {
                            board[i][j] = monoColor
                        }
                    }
                }
            }
            BonusType.BOMB -> {
                // Xóa vùng 3x3 xung quanh trung tâm (hoặc vị trí ngẫu nhiên có khối)
                val centerX = Random.nextInt(2, rows - 2)
                val centerY = Random.nextInt(2, cols - 2)
                for (i in centerX - 1..centerX + 1) {
                    for (j in centerY - 1..centerY + 1) {
                        board[i][j] = 0L
                    }
                }
            }
            BonusType.LIGHTNING -> {
                // Xóa hàng và cột giao nhau tại một điểm ngẫu nhiên
                val rowIdx = Random.nextInt(rows)
                val colIdx = Random.nextInt(cols)
                for (j in 0 until cols) board[rowIdx][j] = 0L
                for (i in 0 until rows) board[i][colIdx] = 0L
            }
            BonusType.COLOR_BOMB -> {
                // Xóa tất cả các ô có cùng một màu cụ thể (màu xuất hiện nhiều nhất hoặc ngẫu nhiên)
                val colors = mutableMapOf<Long, Int>()
                for (i in 0 until rows) {
                    for (j in 0 until cols) {
                        if (board[i][j] != 0L) {
                            colors[board[i][j]] = colors.getOrDefault(board[i][j], 0) + 1
                        }
                    }
                }
                if (colors.isNotEmpty()) {
                    val targetColor = colors.maxByOrNull { it.value }?.key ?: 0L
                    for (i in 0 until rows) {
                        for (j in 0 until cols) {
                            if (board[i][j] == targetColor) {
                                board[i][j] = 0L
                            }
                        }
                    }
                }
            }
            BonusType.GHOST -> {
                // Hiệu ứng "Ghost": Ở đây đơn giản là xóa 3 hàng dưới cùng để dọn đường
                for (i in rows - 3 until rows) {
                    for (j in 0 until cols) {
                        board[i][j] = 0L
                    }
                }
            }
        }

        // Giả lập animation kết thúc sau một khoảng thời gian ngắn
        onEffectComplete()
        
        return type.bonusScore
    }
}
