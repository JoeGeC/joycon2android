package com.joegec.joycon2android.battery

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.PlayerNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class LowBatteryWatchTest {

    private val watch = LowBatteryWatch()
    private val one = setOf(20)
    private val two = setOf(20, 10)
    private val none = emptyList<ControllerBattery>()

    private fun left(percent: Int) = ControllerBattery("L", PlayerNumber.P1, ControllerModel.JOYCON_LEFT, "Joy-Con 2 (L)", percent)
    private fun right(percent: Int) = ControllerBattery("R", PlayerNumber.P1, ControllerModel.JOYCON_RIGHT, "Joy-Con 2 (R)", percent)

    @Test
    fun `a controller above the warning is not warned about`() {
        assertEquals(none, watch.newlyLow(listOf(left(25)), one))
    }

    @Test
    fun `falling to the warning warns`() {
        watch.newlyLow(listOf(left(25)), one)

        assertEquals(listOf(left(20)), watch.newlyLow(listOf(left(20)), one))
    }

    @Test
    fun `a controller warns once while it stays low`() {
        watch.newlyLow(listOf(left(20)), one)

        assertEquals(none, watch.newlyLow(listOf(left(15)), one))
    }

    @Test
    fun `each controller warns for itself`() {
        watch.newlyLow(listOf(left(15), right(30)), one)

        assertEquals(listOf(right(20)), watch.newlyLow(listOf(left(15), right(20)), one))
    }

    @Test
    fun `falling to the second warning warns again`() {
        watch.newlyLow(listOf(left(20)), two)
        watch.newlyLow(listOf(left(15)), two)

        assertEquals(listOf(left(10)), watch.newlyLow(listOf(left(10)), two))
    }

    @Test
    fun `arriving below both warnings warns once`() {
        assertEquals(listOf(left(5)), watch.newlyLow(listOf(left(5)), two))
        assertEquals(none, watch.newlyLow(listOf(left(5)), two))
    }

    @Test
    fun `rising between the warnings does not re-arm the first`() {
        watch.newlyLow(listOf(left(10)), two)
        watch.newlyLow(listOf(left(15)), two)

        assertEquals(none, watch.newlyLow(listOf(left(10)), two))
    }

    @Test
    fun `charging above every warning re-arms them`() {
        watch.newlyLow(listOf(left(15)), one)
        watch.newlyLow(listOf(left(60)), one)

        assertEquals(listOf(left(20)), watch.newlyLow(listOf(left(20)), one))
    }

    @Test
    fun `reconnecting still low warns again`() {
        watch.newlyLow(listOf(left(15)), one)
        watch.newlyLow(none, one)

        assertEquals(listOf(left(15)), watch.newlyLow(listOf(left(15)), one))
    }

    @Test
    fun `raising the warning past a controller's charge warns`() {
        watch.newlyLow(listOf(left(25)), one)

        assertEquals(listOf(left(25)), watch.newlyLow(listOf(left(25)), setOf(30)))
    }

    @Test
    fun `after a reset a low controller warns again`() {
        watch.newlyLow(listOf(left(15)), one)
        watch.reset()

        assertEquals(listOf(left(15)), watch.newlyLow(listOf(left(15)), one))
    }
}
