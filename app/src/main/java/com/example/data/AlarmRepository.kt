package com.example.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val presetDao: PresetDao
) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val activeAlarms: Flow<List<AlarmEntity>> = alarmDao.getActiveAlarms()
    val presets: Flow<List<IntervalPresetEntity>> = presetDao.getAllPresets()

    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    suspend fun getUpcomingAlarmsSync(currentTime: Long): List<AlarmEntity> =
        alarmDao.getUpcomingAlarmsSync(currentTime)

    suspend fun createAlarm(
        durationMinutes: Float,
        label: String = "Interval Alarm"
    ): Long {
        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000).toLong()
        val alarm = AlarmEntity(
            triggerTimeMillis = triggerTime,
            createdTimeMillis = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            label = label,
            isEnabled = true,
            isFiring = false,
            isDismissed = false
        )
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun createAlarmAt(
        triggerTimeMillis: Long,
        label: String = "Alarm"
    ): Long {
        val remainingMillis = triggerTimeMillis - System.currentTimeMillis()
        val durationMinutes = (remainingMillis.toFloat() / (60 * 1000f)).coerceAtLeast(1f)
        val alarm = AlarmEntity(
            triggerTimeMillis = triggerTimeMillis,
            createdTimeMillis = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            label = label,
            isEnabled = true,
            isFiring = false,
            isDismissed = false
        )
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun snoozeAlarm(alarmId: Long, snoozeMinutes: Int = 10): Long {
        val existing = alarmDao.getAlarmById(alarmId)
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000).toLong()
        val label = existing?.label ?: "Snoozed Alarm"
        
        // Mark current as dismissed
        alarmDao.setAlarmDismissed(alarmId, true)

        // Create new snoozed alarm
        val snoozedAlarm = AlarmEntity(
            triggerTimeMillis = triggerTime,
            createdTimeMillis = System.currentTimeMillis(),
            durationMinutes = snoozeMinutes.toFloat(),
            label = "$label (Snoozed)",
            isEnabled = true,
            isFiring = false,
            isDismissed = false
        )
        return alarmDao.insertAlarm(snoozedAlarm)
    }

    suspend fun deleteAlarm(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun setAlarmDismissed(id: Long) = alarmDao.setAlarmDismissed(id, true)

    suspend fun setAlarmFiring(id: Long, isFiring: Boolean) = alarmDao.setAlarmFiring(id, isFiring)

    suspend fun setAlarmEnabled(id: Long, enabled: Boolean) = alarmDao.setAlarmEnabled(id, enabled)

    suspend fun clearDismissedAlarms() = alarmDao.clearDismissedAlarms()

    // Preset management
    suspend fun addPreset(label: String, durationMinutes: Float) {
        val count = presetDao.getCount()
        presetDao.insertPreset(
            IntervalPresetEntity(
                label = label,
                durationMinutes = durationMinutes,
                sortOrder = count
            )
        )
    }

    suspend fun deletePreset(id: Int) = presetDao.deletePresetById(id)

    suspend fun resetPresetsToDefault() {
        presetDao.deleteAllPresets()
        AppDatabase.populateDefaults(presetDao)
    }

    suspend fun ensureDefaultPresets() {
        val existing = presetDao.getPresetsSync()
        if (existing.isEmpty()) {
            AppDatabase.populateDefaults(presetDao)
        } else {
            // Deduplicate any presets with duplicate label and duration
            val seenKeys = mutableSetOf<String>()
            for (preset in existing) {
                val key = "${preset.label}_${preset.durationMinutes}"
                if (key in seenKeys) {
                    presetDao.deletePresetById(preset.id)
                } else {
                    seenKeys.add(key)
                }
            }
        }
    }
}
