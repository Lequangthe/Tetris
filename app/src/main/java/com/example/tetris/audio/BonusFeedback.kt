package com.example.tetris.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.tetris.logic.BonusType

class BonusFeedback(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = mutableMapOf<BonusType, Int>()

    init {
        // Giả sử các file âm thanh này tồn tại trong res/raw/
        // Nếu chưa có, có thể dùng ToneGenerator hoặc hướng dẫn user thêm vào.
        // sounds[BonusType.MONO_FILL] = soundPool.load(context, R.raw.ding, 1)
        // sounds[BonusType.BOMB] = soundPool.load(context, R.raw.boom, 1)
        // sounds[BonusType.LIGHTNING] = soundPool.load(context, R.raw.zap, 1)
        // sounds[BonusType.GHOST] = soundPool.load(context, R.raw.whoosh, 1)
    }

    fun playSound(type: BonusType) {
        val soundId = sounds[type]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Fallback: Nếu không có file âm thanh, in log hoặc dùng âm thanh hệ thống
            println("Playing sound for bonus: ${type.displayName}")
        }
    }

    fun vibrate(type: BonusType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        when (type) {
            BonusType.BOMB -> {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            BonusType.LIGHTNING -> {
                val pattern = longArrayOf(0, 50, 50, 50)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
            }
            else -> {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }
}
