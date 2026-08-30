package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Device
import com.example.data.model.MeterReading
import com.example.data.model.ConsumptionHistory
import com.example.data.repository.ElectricityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ElectricityViewModel(
    application: Application,
    private val repository: ElectricityRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val devices: StateFlow<List<Device>> = repository.allDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val meterReadings: StateFlow<List<MeterReading>> = repository.allMeterReadings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val consumptionHistory: StateFlow<List<ConsumptionHistory>> = repository.allConsumptionHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived states
    val devicesMonthlyTotal: StateFlow<Double> = devices.map { list ->
        list.sumOf { (it.powerW / 1000.0) * it.hoursPerDay * it.daysPerMonth * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lastReadingConsumption: StateFlow<Double> = meterReadings.map { list ->
        if (list.isNotEmpty()) {
            val latest = list.first()
            latest.currentReading - latest.previousReading
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalMonthlyConsumption: StateFlow<Double> = combine(devicesMonthlyTotal, lastReadingConsumption) { dev, reading ->
        dev + reading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val estimatedCost: StateFlow<Double> = combine(totalMonthlyConsumption, settings) { consumption, setts ->
        consumption * setts.pricePerKWh
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyAverage: StateFlow<Double> = totalMonthlyConsumption.map {
        it / 30.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Add device
    fun addDevice(name: String, powerW: Double, hoursPerDay: Double, daysPerMonth: Int, quantity: Int) {
        viewModelScope.launch {
            val device = Device(
                name = name,
                powerW = powerW,
                hoursPerDay = hoursPerDay,
                daysPerMonth = daysPerMonth,
                quantity = quantity
            )
            repository.insertDevice(device)
            // Show interstitial ad trigger placeholder
            incrementCalculationCounter()
        }
    }

    // Edit device
    fun updateDevice(device: Device) {
        viewModelScope.launch {
            repository.updateDevice(device)
        }
    }

    // Duplicate device
    fun duplicateDevice(device: Device) {
        viewModelScope.launch {
            val duplicated = device.copy(
                id = 0, // Generated automatically
                name = "${device.name} (نسخة)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertDevice(duplicated)
        }
    }

    // Delete device
    fun deleteDevice(device: Device) {
        viewModelScope.launch {
            repository.deleteDevice(device)
        }
    }

    // Add meter reading
    fun addMeterReading(prev: Double, curr: Double, days: Int) {
        viewModelScope.launch {
            val reading = MeterReading(
                previousReading = prev,
                currentReading = curr,
                days = days
            )
            repository.insertMeterReading(reading)

            // Save to consumption history automatically
            val consumption = curr - prev
            val price = settings.value.pricePerKWh
            val cost = consumption * price
            val monthString = getCurrentMonthString()

            val historyItem = ConsumptionHistory(
                month = monthString,
                totalConsumption = consumption,
                totalCost = cost
            )
            repository.insertConsumptionHistory(historyItem)
            incrementCalculationCounter()
        }
    }

    // Delete meter reading
    fun deleteMeterReading(reading: MeterReading) {
        viewModelScope.launch {
            repository.deleteMeterReading(reading)
        }
    }

    // Update settings
    fun updatePrice(price: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(pricePerKWh = price))
        }
    }

    fun updateSettings(price: Double, currency: String, currencySymbol: String, darkMode: Boolean, theme: String) {
        viewModelScope.launch {
            repository.saveSettings(price, currency, currencySymbol, darkMode, theme)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(darkMode = enabled, theme = if (enabled) "dark" else "light"))
        }
    }

    // Danger zone
    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAppData()
        }
    }

    private fun getCurrentMonthString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    // Ad stub counter
    private var calculationCounter = 0
    val showAdTriggerFlow = MutableSharedFlow<String>(replay = 0)

    private fun incrementCalculationCounter() {
        calculationCounter++
        if (calculationCounter % 5 == 0) {
            viewModelScope.launch {
                showAdTriggerFlow.emit("Interstitial Ad Triggered (Placeholder)")
            }
        }
    }

    // Input Validation
    fun validateInput(value: String, type: String, min: Double, max: Double): String? {
        if (value.isBlank()) {
            return "هذا الحقل مطلوب"
        }
        val number = value.toDoubleOrNull()
        if (number == null) {
            return "يجب إدخال رقم صحيح"
        }
        if (number < 0) {
            return "لا يمكن إدخال قيم سالبة"
        }
        if (number == 0.0 && type != "quantity") {
            return "القيمة لا يمكن أن تكون صفراً"
        }
        if (number < min || number > max) {
            return "القيمة يجب أن تكون بين ${formatNumber(min)} و ${formatNumber(max)}"
        }
        return null
    }

    private fun formatNumber(d: Double): String {
        return if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
    }
}

class ElectricityViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElectricityViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val repository = ElectricityRepository(
                db.appSettingsDao(),
                db.deviceDao(),
                db.meterReadingDao(),
                db.consumptionHistoryDao()
            )
            @Suppress("UNCHECKED_CAST")
            return ElectricityViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
