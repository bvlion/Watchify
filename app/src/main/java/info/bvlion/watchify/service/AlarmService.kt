package info.bvlion.watchify.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.CombinedVibration
import android.os.IBinder
import android.os.VibrationEffect
import android.os.VibratorManager
import info.bvlion.watchify.R

class AlarmService : Service() {

    private lateinit var vibratorManager: VibratorManager
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private var defaultVolume: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC, 100,
            AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND)

        mediaPlayer = MediaPlayer.create(this, R.raw.alert)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(1f, 1f)
        mediaPlayer.start()

        vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrationEffect = VibrationEffect.createWaveform(
            longArrayOf(0, 800, 250, 50),
            2
        )
        val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
        vibratorManager.vibrate(combinedVibration)
    }

    override fun onDestroy() {
        super.onDestroy()
        vibratorManager.cancel()
        mediaPlayer.stop()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume,
            AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND)
    }
}