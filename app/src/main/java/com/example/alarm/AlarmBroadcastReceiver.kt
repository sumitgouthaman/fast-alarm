package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.QuickIntervalApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("AlarmBroadcastReceiver", "Received action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "QuickInterval:AlarmBroadcastWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 1 minute CPU wakeLock

        val app = context.applicationContext as? QuickIntervalApp
        val repository = app?.repository

        when (action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Device rebooted: reschedule all upcoming active alarms
                val scheduler = AlarmScheduler(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val upcoming = repository?.getUpcomingAlarmsSync(currentTime) ?: emptyList()
                        for (alarm in upcoming) {
                            scheduler.scheduleAlarm(alarm.id, alarm.triggerTimeMillis, alarm.label)
                        }
                        Log.d("AlarmBroadcastReceiver", "Rescheduled ${upcoming.size} alarms after reboot")
                    } catch (e: Exception) {
                        Log.e("AlarmBroadcastReceiver", "Failed to reschedule on boot", e)
                    } finally {
                        if (wakeLock.isHeld) wakeLock.release()
                    }
                }
            }
            AlarmScheduler.ACTION_TRIGGER_ALARM -> {
                val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
                val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Interval Alarm"
                Log.d("AlarmBroadcastReceiver", "Triggering alarm: id=$alarmId, label=$label")

                // Acquire screen wakeup lock to ensure device screen turns on
                try {
                    @Suppress("DEPRECATION")
                    val screenWakeLock = powerManager.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                        "QuickInterval:AlarmScreenWakeLock"
                    )
                    screenWakeLock.acquire(10 * 1000L) // 10 seconds screen on
                } catch (e: Exception) {
                    Log.e("AlarmBroadcastReceiver", "Failed to acquire screen wakeLock", e)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (alarmId != -1L) {
                            repository?.setAlarmFiring(alarmId, true)
                        }
                    } catch (e: Exception) {
                        Log.e("AlarmBroadcastReceiver", "Failed to update alarm firing state", e)
                    }
                }

                // Start Foreground Ringing Service
                val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                    this.action = AlarmRingingService.ACTION_START_RINGING
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
                }
                ContextCompat.startForegroundService(context, serviceIntent)

                // Launch full-screen ringing activity directly
                val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                try {
                    context.startActivity(activityIntent)
                } catch (e: Exception) {
                    Log.e("AlarmBroadcastReceiver", "Failed to start activity directly, relying on fullScreenIntent", e)
                }

                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
            else -> {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }
}
