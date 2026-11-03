package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmEntity::class, IntervalPresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quick_interval_alarms.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDefaults(dao: PresetDao) {
            val defaultPresets = listOf(
                IntervalPresetEntity(label = "30m", durationMinutes = 30f, sortOrder = 0),
                IntervalPresetEntity(label = "1h", durationMinutes = 60f, sortOrder = 1),
                IntervalPresetEntity(label = "1.5h", durationMinutes = 90f, sortOrder = 2),
                IntervalPresetEntity(label = "2h", durationMinutes = 120f, sortOrder = 3),
                IntervalPresetEntity(label = "2.5h", durationMinutes = 150f, sortOrder = 4),
                IntervalPresetEntity(label = "3h", durationMinutes = 180f, sortOrder = 5)
            )
            dao.insertPresets(defaultPresets)
        }
    }
}
