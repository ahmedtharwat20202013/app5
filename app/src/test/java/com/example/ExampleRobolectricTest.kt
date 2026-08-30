package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("حاسب الكهرباء", appName)
    }

    @Test
    fun `verify electricity formulas`() {
        // Inputs
        val powerWatts = 2000.0
        val hoursPerDay = 3.5
        val quantity = 2
        val daysPerMonth = 30
        val pricePerKWh = 0.75

        // Formulas
        val dailyConsumptionKWh = (powerWatts / 1000.0) * hoursPerDay * quantity
        val monthlyConsumptionKWh = dailyConsumptionKWh * daysPerMonth
        val monthlyCost = monthlyConsumptionKWh * pricePerKWh

        // Assertions
        assertEquals(14.0, dailyConsumptionKWh, 0.001)
        assertEquals(420.0, monthlyConsumptionKWh, 0.001)
        assertEquals(315.0, monthlyCost, 0.001)
    }
}
