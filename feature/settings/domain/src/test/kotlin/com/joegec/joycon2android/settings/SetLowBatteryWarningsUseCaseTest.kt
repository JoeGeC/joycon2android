package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SetLowBatteryWarningsUseCaseTest {

    private var stored: Pair<Int, Int>? = null
    private val setWarnings = SetLowBatteryWarningsUseCase(object : LowBatteryAlertRepository {
        override val alert: Flow<LowBatteryAlert> = emptyFlow()
        override suspend fun setNotify(enabled: Boolean) = Unit
        override suspend fun setVibrate(enabled: Boolean) = Unit
        override suspend fun setWarningPercents(first: Int, final: Int) {
            stored = first to final
        }
    })

    @Test
    fun `the higher level is the first warning`() = runBlocking {
        setWarnings(10, 25)

        assertEquals(25 to 10, stored)
    }

    @Test
    fun `levels outside the slider are clamped`() = runBlocking {
        setWarnings(0, 90)

        assertEquals(50 to 5, stored)
    }
}
