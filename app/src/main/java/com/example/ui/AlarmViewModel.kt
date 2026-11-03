package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.QuickIntervalApp
import com.example.alarm.AlarmRingingActivity
import com.example.alarm.AlarmRingingService
import com.example.alarm.AlarmScheduler
import com.example.data.AlarmEntity
import com.example.data.AlarmRepository
import com.example.data.IntervalPresetEntity
import com.example.notifications.UpcomingAlarmsNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AlarmUiState(
    val activeAlarms: List<AlarmEntity> = emptyList(),
    val presets: List<IntervalPresetEntity> = emptyList(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val isRinging: Boolean = false,
    val ringingAlarmLabel: String? = null,
    val canScheduleExactAlarms: Boolean = true
)

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository = (application as QuickIntervalApp).repository
    private val scheduler: AlarmScheduler = AlarmScheduler(application)
    private val upcomingNotificationManager = UpcomingAlarmsNotificationManager(application)

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val uiState: StateFlow<AlarmUiState> = combine(
        repository.activeAlarms,
        repository.presets,
        _currentTimeMillis,
        AlarmRingingService.isCurrentlyRinging,
        AlarmRingingService.ringingAlarmLabel
    ) { activeAlarms, presets, currentTime, isRinging, ringingLabel ->
        AlarmUiState(
            activeAlarms = activeAlarms,
            presets = presets,
            currentTimeMillis = currentTime,
            isRinging = isRinging,
            ringingAlarmLabel = ringingLabel,
            canScheduleExactAlarms = scheduler.canScheduleExactAlarms()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmUiState()
    )

    init {
        // Real-time ticking loop to update countdowns smoothly every second
        viewModelScope.launch {
            while (true) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        // Keep persistent upcoming alarms notification synchronized
        viewModelScope.launch {
            repository.activeAlarms.collect { alarms ->
                upcomingNotificationManager.updateUpcomingAlarmsNotification(alarms)
            }
        }
    }

    fun setQuickIntervalAlarm(durationMinutes: Float, customLabel: String? = null) {
        viewModelScope.launch {
            val label = customLabel ?: "Interval (${formatDuration(durationMinutes)})"
            val alarmId = repository.createAlarm(durationMinutes, label)
            val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L).toLong()

            scheduler.scheduleAlarm(alarmId, triggerTime, label)

            val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(triggerTime))
            _snackbarMessage.emit("Alarm set for $timeFormatted (in ${formatDuration(durationMinutes)})")
        }
    }

    fun setExactTimeAlarm(triggerTimeMillis: Long, label: String) {
        viewModelScope.launch {
            val alarmId = repository.createAlarmAt(triggerTimeMillis, label)
            scheduler.scheduleAlarm(alarmId, triggerTimeMillis, label)

            val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(triggerTimeMillis))
            val remainingMillis = (triggerTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)
            val hoursLeft = remainingMillis / (1000 * 60 * 60)
            val minsLeft = (remainingMillis / (1000 * 60)) % 60
            val relativeStr = if (hoursLeft > 0) "${hoursLeft}h ${minsLeft}m" else "${minsLeft}m"

            _snackbarMessage.emit("Alarm set for $timeFormatted (in $relativeStr)")
        }
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            scheduler.cancelAlarm(alarm.id)
            repository.deleteAlarm(alarm.id)
            _snackbarMessage.emit("Alarm cancelled")
        }
    }

    fun toggleAlarmEnabled(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.setAlarmEnabled(alarm.id, enabled)
            if (enabled) {
                // If trigger time is in the past, push it to new interval
                val triggerTime = if (alarm.triggerTimeMillis > System.currentTimeMillis()) {
                    alarm.triggerTimeMillis
                } else {
                    System.currentTimeMillis() + (alarm.durationMinutes * 60 * 1000L).toLong()
                }
                scheduler.scheduleAlarm(alarm.id, triggerTime, alarm.label)
                _snackbarMessage.emit("Alarm armed")
            } else {
                scheduler.cancelAlarm(alarm.id)
                _snackbarMessage.emit("Alarm paused")
            }
        }
    }

    fun testAlarmNow() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val intent = Intent(context, AlarmRingingService::class.java).apply {
                action = AlarmRingingService.ACTION_START_RINGING
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, 999999L)
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, "Test Interval Alarm")
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)

            val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, 999999L)
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, "Test Interval Alarm")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            context.startActivity(activityIntent)
        }
    }

    fun addPreset(label: String, durationMinutes: Float) {
        viewModelScope.launch {
            repository.addPreset(label, durationMinutes)
            _snackbarMessage.emit("Preset '$label' added")
        }
    }

    fun deletePreset(id: Int) {
        viewModelScope.launch {
            repository.deletePreset(id)
            _snackbarMessage.emit("Preset removed")
        }
    }

    fun resetPresetsToDefault() {
        viewModelScope.launch {
            repository.resetPresetsToDefault()
            _snackbarMessage.emit("Default presets restored")
        }
    }

    private fun formatDuration(minutes: Float): String {
        return if (minutes >= 60f) {
            val h = minutes / 60f
            if (h % 1f == 0f) "${h.toInt()}h" else "${h}h"
        } else {
            "${minutes.toInt()}m"
        }
    }
}
