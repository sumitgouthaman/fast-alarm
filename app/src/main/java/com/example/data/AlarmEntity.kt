package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerTimeMillis: Long,
    val createdTimeMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Float,
    val label: String,
    val isEnabled: Boolean = true,
    val isFiring: Boolean = false,
    val isDismissed: Boolean = false
)
