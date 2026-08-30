package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val pricePerKWh: Double = 0.50,
    val currency: String = "EGP",
    val currencySymbol: String = "ج.م",
    val darkMode: Boolean = false,
    val theme: String = "light"
)

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val powerW: Double,
    val hoursPerDay: Double,
    val daysPerMonth: Int,
    val quantity: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meter_readings")
data class MeterReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val previousReading: Double,
    val currentReading: Double,
    val days: Int,
    val calculatedOn: Long = System.currentTimeMillis()
)

@Entity(tableName = "consumption_history")
data class ConsumptionHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String,
    val totalConsumption: Double,
    val totalCost: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
