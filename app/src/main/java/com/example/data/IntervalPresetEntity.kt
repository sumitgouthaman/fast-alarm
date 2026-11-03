package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interval_presets")
data class IntervalPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val durationMinutes: Float,
    val sortOrder: Int
)
