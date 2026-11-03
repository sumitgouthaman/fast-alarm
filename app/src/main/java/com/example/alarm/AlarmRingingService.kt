package com.example.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.QuickIntervalApp
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarmId: Long = -1L
    private var currentAlarmLabel: String = "Interval Alarm"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "QuickInterval:AlarmRingingServiceWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max wakeLock timeout

        initVibrator()
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_RINGING
        currentAlarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, currentAlarmId) ?: currentAlarmId
        currentAlarmLabel = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: currentAlarmLabel

        Log.d("AlarmRingingService", "onStartCommand: action=$action, id=$currentAlarmId")

        when (action) {
            ACTION_START_RINGING -> {
                startForegroundNotification()
                startAlarmSound()
                startVibration()
                _isCurrentlyRinging.value = true
                _ringingAlarmId.value = currentAlarmId
                _ringingAlarmLabel.value = currentAlarmLabel
            }
            ACTION_DISMISS -> {
                dismissAlarm()
            }
            ACTION_SNOOZE -> {
                val minutes = intent?.getIntExtra(EXTRA_SNOOZE_MINUTES, 10) ?: 10
                snoozeAlarm(minutes)
            }
        }

        return START_STICKY
    }

    private fun startForegroundNotification() {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, currentAlarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, currentAlarmLabel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Action
        val dismissIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, currentAlarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            1002,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, currentAlarmId)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            1003,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, QuickIntervalApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $currentAlarmLabel")
            .setSubText("Fast Alarm")
            .setContentText("Interval alarm is ringing! Slide or tap to dismiss.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "Snooze (+10m)", snoozePendingIntent)
            .addAction(0, "Dismiss", dismissPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startAlarmSound() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                return
            }
            mediaPlayer?.release()

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Set alarm volume to a high audible level
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            if (currentVol < maxVol * 0.7) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (maxVol * 0.85).toInt(), 0)
            }

            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmRingingService, alarmUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
            Log.d("AlarmRingingService", "MediaPlayer started playing alarm sound")
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to start MediaPlayer for alarm", e)
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 800, 300, 800, 300, 1200)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // repeat at index 0
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to start vibration", e)
        }
    }

    private fun stopRinging() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error stopping MediaPlayer", e)
        }

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Error canceling vibrator", e)
        }

        _isCurrentlyRinging.value = false
        _ringingAlarmId.value = null
        _ringingAlarmLabel.value = null
    }

    private fun dismissAlarm() {
        stopRinging()
        val app = applicationContext as? QuickIntervalApp
        val repo = app?.repository
        val idToDismiss = currentAlarmId

        CoroutineScope(Dispatchers.IO).launch {
            if (idToDismiss != -1L) {
                repo?.setAlarmDismissed(idToDismiss)
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snoozeAlarm(minutes: Int) {
        stopRinging()
        val app = applicationContext as? QuickIntervalApp
        val repo = app?.repository
        val idToSnooze = currentAlarmId
        val scheduler = AlarmScheduler(this)

        CoroutineScope(Dispatchers.IO).launch {
            if (idToSnooze != -1L) {
                val newAlarmId = repo?.snoozeAlarm(idToSnooze, minutes) ?: -1L
                if (newAlarmId != -1L) {
                    val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
                    scheduler.scheduleAlarm(newAlarmId, triggerTime, "$currentAlarmLabel (Snoozed)")
                }
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopRinging()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 9001
        const val ACTION_START_RINGING = "com.example.quickalarm.ACTION_START_RINGING"
        const val ACTION_DISMISS = "com.example.quickalarm.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.quickalarm.ACTION_SNOOZE"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

        private val _isCurrentlyRinging = MutableStateFlow(false)
        val isCurrentlyRinging: StateFlow<Boolean> = _isCurrentlyRinging.asStateFlow()

        private val _ringingAlarmId = MutableStateFlow<Long?>(null)
        val ringingAlarmId: StateFlow<Long?> = _ringingAlarmId.asStateFlow()

        private val _ringingAlarmLabel = MutableStateFlow<String?>(null)
        val ringingAlarmLabel: StateFlow<String?> = _ringingAlarmLabel.asStateFlow()
    }
}
