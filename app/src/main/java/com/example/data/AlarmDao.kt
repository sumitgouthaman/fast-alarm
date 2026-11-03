package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY triggerTimeMillis ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isDismissed = 0 AND isEnabled = 1 ORDER BY triggerTimeMillis ASC")
    fun getActiveAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isDismissed = 0 AND isEnabled = 1 AND triggerTimeMillis > :currentTime ORDER BY triggerTimeMillis ASC")
    suspend fun getUpcomingAlarmsSync(currentTime: Long): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE alarms SET isDismissed = :dismissed, isFiring = 0 WHERE id = :id")
    suspend fun setAlarmDismissed(id: Long, dismissed: Boolean = true)

    @Query("UPDATE alarms SET isFiring = :firing WHERE id = :id")
    suspend fun setAlarmFiring(id: Long, firing: Boolean)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM alarms WHERE isDismissed = 1")
    suspend fun clearDismissedAlarms()
}
