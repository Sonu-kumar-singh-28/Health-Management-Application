package com.manage.health.healthtrackerapplication.ui.utils
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.provider.MediaStore
import android.util.Log
import com.manage.health.healthtrackerapplication.R


class SoundManager(private val  context: Context) {


    companion object {
        private const val TAG = "SoundManager"
        private const val MAX_STREAMS = 5
    }

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()


    init {
        initializeSoundPool()
        loadSounds()
    }


    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun loadSounds() {
        try {
            soundPool?.let { pool ->
                soundMap["water"] = pool.load(context, R.raw.water_drop, 1)
                soundMap["step"] = pool.load(context, R.raw.step_sound, 1)
                soundMap["sleep"] = pool.load(context, R.raw.sleep_chime, 1)
                soundMap["success"] = pool.load(context, R.raw.success_chime, 1)
                soundMap["notification"] = pool.load(context, R.raw.notification_sound, 1)

                soundMap["rain"] = pool.load(context, R.raw.water_drop, 1)
                soundMap["ocean"] = pool.load(context, R.raw.sleep_chime, 1)
                soundMap["forest"] = pool.load(context, R.raw.success_chime, 1)
                soundMap["white_noise"] = pool.load(context, R.raw.notification_sound, 1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not load sound files: ${e.message}")
        }
    }

    fun playSound(soundKey: String) {

        try {
            val soundId = soundMap[soundKey] ?: 0
            if (soundId != 0) {
                soundPool?.play(soundId, 0.7f, 0.7f, 1, 0, 1f)
            }
        }catch (e: Exception){
            Log.d(TAG, "Sound $soundKey' Not Loaded or not Found")
        }
    }

    fun playWaterSound() {
        playSound("water")
    }

    fun playStepSound() {
        playSound("step")
    }

    fun playSleepSound() {
        playSound("sleep")
    }

    fun playSuccessSound() {
        playSound("success")
    }

    fun playNotificationSound() {
        playSound("notification")
    }

    fun playRainSound() {
        playSound("rain")
    }

    fun playOceanSound() {
        playSound("ocean")
    }

    fun playForestSound() {
        playSound("forest")
    }

    fun playWhiteNoiseSound() {
        playSound("white_noise")
    }

    fun release(){
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}