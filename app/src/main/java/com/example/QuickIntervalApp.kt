package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import com.example.data.AppDatabase
import com.example.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickIntervalApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: AlarmRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = AlarmRepository(database.alarmDao(), database.presetDao())

        createNotificationChannels()

        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureDefaultPresets()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // High Priority Alarm Channel
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Interval Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority sound and full-screen alerts for interval alarms"
                setSound(Settings.System.DEFAULT_ALARM_ALERT_URI, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 1000)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager?.createNotificationChannel(alarmChannel)

            // Low Priority Persistent Upcoming Alarms Channel
            val upcomingChannel = NotificationChannel(
                UPCOMING_ALARMS_CHANNEL_ID,
                "Active Alarms Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows an ongoing status of your upcoming armed interval alarms"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(upcomingChannel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "quick_interval_alarm_channel"
        const val UPCOMING_ALARMS_CHANNEL_ID = "quick_interval_upcoming_channel"
        lateinit var instance: QuickIntervalApp
            private set
    }
}
