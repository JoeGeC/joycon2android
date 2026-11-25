package com.joegec.joycon2android.connectionloss

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.PlayerNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionLossWatchTest {

    private val watch = ConnectionLossWatch()
    private val right = LostController("R", PlayerNumber.P1, ControllerModel.JOYCON_RIGHT, "Joy-Con 2 (R)", false)

    @Test
    fun `a drop is reported`() {
        assertEquals(listOf(right), watch.update(listOf(right), emptySet()).lost)
    }

    @Test
    fun `a drop still waiting is reported once`() {
        watch.update(listOf(right), emptySet())

        assertEquals(emptyList<LostController>(), watch.update(listOf(right), emptySet()).lost)
    }

    @Test
    fun `a dropped controller connecting again has returned`() {
        watch.update(listOf(right), emptySet())

        assertEquals(setOf("R"), watch.update(emptyList(), setOf("R")).returned)
    }

    @Test
    fun `a controller connected all along has not returned`() {
        assertEquals(emptySet<String>(), watch.update(emptyList(), setOf("R")).returned)
    }

    @Test
    fun `a controller removed while lost is forgotten`() {
        watch.update(listOf(right), emptySet())
        watch.update(emptyList(), emptySet())

        assertEquals(emptySet<String>(), watch.update(emptyList(), setOf("R")).returned)
    }

    @Test
    fun `dropping again after returning is reported again`() {
        watch.update(listOf(right), emptySet())
        watch.update(emptyList(), setOf("R"))

        assertEquals(listOf(right), watch.update(listOf(right), emptySet()).lost)
    }
}
