package com.example.data.local

import androidx.room.*
import com.example.data.model.AppSettings
import com.example.data.model.Device
import com.example.data.model.MeterReading
import com.example.data.model.ConsumptionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY createdAt DESC")
    fun getAllDevices(): Flow<List<Device>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    @Update
    suspend fun updateDevice(device: Device)

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
}

@Dao
interface MeterReadingDao {
    @Query("SELECT * FROM meter_readings ORDER BY calculatedOn DESC")
    fun getAllMeterReadings(): Flow<List<MeterReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: MeterReading): Long

    @Delete
    suspend fun deleteReading(reading: MeterReading)

    @Query("DELETE FROM meter_readings")
    suspend fun deleteAllReadings()
}

@Dao
interface ConsumptionHistoryDao {
    @Query("SELECT * FROM consumption_history ORDER BY month DESC, lastUpdated DESC")
    fun getHistory(): Flow<List<ConsumptionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConsumptionHistory): Long

    @Delete
    suspend fun deleteHistory(history: ConsumptionHistory)

    @Query("DELETE FROM consumption_history")
    suspend fun deleteAllHistory()
}
