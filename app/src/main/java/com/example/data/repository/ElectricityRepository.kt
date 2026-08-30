package com.example.data.repository

import com.example.data.local.AppSettingsDao
import com.example.data.local.DeviceDao
import com.example.data.local.MeterReadingDao
import com.example.data.local.ConsumptionHistoryDao
import com.example.data.model.AppSettings
import com.example.data.model.Device
import com.example.data.model.MeterReading
import com.example.data.model.ConsumptionHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ElectricityRepository(
    private val appSettingsDao: AppSettingsDao,
    private val deviceDao: DeviceDao,
    private val meterReadingDao: MeterReadingDao,
    private val consumptionHistoryDao: ConsumptionHistoryDao
) {
    val settings: Flow<AppSettings> = appSettingsDao.getSettings().map { it ?: AppSettings() }
    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()
    val allMeterReadings: Flow<List<MeterReading>> = meterReadingDao.getAllMeterReadings()
    val allConsumptionHistory: Flow<List<ConsumptionHistory>> = consumptionHistoryDao.getHistory()

    suspend fun saveSettings(price: Double, currency: String, currencySymbol: String, darkMode: Boolean, theme: String) {
        appSettingsDao.insertSettings(
            AppSettings(
                id = 1,
                pricePerKWh = price,
                currency = currency,
                currencySymbol = currencySymbol,
                darkMode = darkMode,
                theme = theme
            )
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        appSettingsDao.insertSettings(settings)
    }

    suspend fun insertDevice(device: Device): Long {
        return deviceDao.insertDevice(device)
    }

    suspend fun updateDevice(device: Device) {
        deviceDao.updateDevice(device)
    }

    suspend fun deleteDevice(device: Device) {
        deviceDao.deleteDevice(device)
    }

    suspend fun insertMeterReading(reading: MeterReading): Long {
        return meterReadingDao.insertReading(reading)
    }

    suspend fun deleteMeterReading(reading: MeterReading) {
        meterReadingDao.deleteReading(reading)
    }

    suspend fun insertConsumptionHistory(history: ConsumptionHistory): Long {
        return consumptionHistoryDao.insertHistory(history)
    }

    suspend fun deleteConsumptionHistory(history: ConsumptionHistory) {
        consumptionHistoryDao.deleteHistory(history)
    }

    suspend fun clearAppData() {
        deviceDao.deleteAllDevices()
        meterReadingDao.deleteAllReadings()
        consumptionHistoryDao.deleteAllHistory()
    }
}
