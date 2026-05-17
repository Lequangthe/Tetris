package com.example.tetris.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log

enum class TetrisSound {
    MOVE, ROTATE, CLEAR, LOCK, GAME_OVER
}

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private val soundMap = mutableMapOf<TetrisSound, Int>()
    
    private val prefs = context.getSharedPreferences("tetris_settings", Context.MODE_PRIVATE)
    
    var volume: Float = prefs.getFloat("volume", 0.5f)
        set(value) {
            field = value
            prefs.edit().putFloat("volume", value).apply()
            updateVolumes()
        }
        
    var bgmEnabled: Boolean = prefs.getBoolean("bgm_enabled", true)
        set(value) {
            field = value
            prefs.edit().putBoolean("bgm_enabled", value).apply()
            if (!value) pauseBGM() else resumeBGM()
        }

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        loadSound(TetrisSound.MOVE, "move")
        loadSound(TetrisSound.ROTATE, "rotate")
        loadSound(TetrisSound.CLEAR, "clear")
        loadSound(TetrisSound.LOCK, "lock")
        loadSound(TetrisSound.GAME_OVER, "gameover")
    }

    private fun loadSound(type: TetrisSound, name: String) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            try {
                soundMap[type] = soundPool?.load(context, resId, 1) ?: 0
            } catch (e: Exception) {
                Log.e("SoundManager", "Error loading sound $name", e)
            }
        }
    }

    fun play(sound: TetrisSound) {
        val soundId = soundMap[sound] ?: return
        if (soundId != 0) {
            soundPool?.play(soundId, volume, volume, 0, 0, 1.0f)
        }
    }

    fun startBGM() {
        if (mediaPlayer != null) return
        val resId = context.resources.getIdentifier("bgm", "raw", context.packageName)
        if (resId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(context, resId).apply {
                    isLooping = true
                    setVolume(volume * 0.5f, volume * 0.5f)
                    if (bgmEnabled) start()
                }
            } catch (e: Exception) {
                Log.e("SoundManager", "Error starting BGM", e)
            }
        }
    }

    private fun updateVolumes() {
        mediaPlayer?.setVolume(volume * 0.5f, volume * 0.5f)
    }

    fun pauseBGM() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resumeBGM() {
        if (!bgmEnabled) return
        val player = mediaPlayer
        if (player == null) {
            // MediaPlayer đã bị release, tạo lại
            startBGM()
        } else if (!player.isPlaying) {
            player.start()
        }
    }

    fun stopBGM() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun release() {
        stopBGM()
        soundPool?.release()
        soundPool = null
    }
}
