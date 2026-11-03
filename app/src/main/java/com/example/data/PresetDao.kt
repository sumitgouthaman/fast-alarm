package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM interval_presets ORDER BY sortOrder ASC")
    fun getAllPresets(): Flow<List<IntervalPresetEntity>>

    @Query("SELECT * FROM interval_presets ORDER BY sortOrder ASC")
    suspend fun getPresetsSync(): List<IntervalPresetEntity>

    @Query("SELECT COUNT(*) FROM interval_presets")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: IntervalPresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<IntervalPresetEntity>)

    @Update
    suspend fun updatePreset(preset: IntervalPresetEntity)

    @Query("DELETE FROM interval_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    @Query("DELETE FROM interval_presets")
    suspend fun deleteAllPresets()
}
