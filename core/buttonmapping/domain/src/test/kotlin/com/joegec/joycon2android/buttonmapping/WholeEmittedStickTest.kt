package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WholeEmittedStickTest {

    private fun following(stick: StickSource) = MappingSource.directionsOf(stick).associateBy { it.direction }

    @Test
    fun `directions that follow one stick the natural way read as that whole stick`() {
        assertEquals(StickSource.RIGHT_STICK, following(StickSource.RIGHT_STICK).wholeEmittedStick(JoyconSide.DUAL))
    }

    @Test
    fun `a lone Joy-Con's stick is emitted as the left stick whichever side it is`() {
        assertEquals(StickSource.LEFT_STICK, following(StickSource.RIGHT_STICK).wholeEmittedStick(JoyconSide.RIGHT))
    }

    @Test
    fun `swapped directions are not a whole stick`() {
        val swapped = following(StickSource.LEFT_STICK) + mapOf(
            StickDirection.UP to MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.DOWN),
            StickDirection.DOWN to MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP),
        )

        assertNull(swapped.wholeEmittedStick(JoyconSide.DUAL))
    }

    @Test
    fun `a direction driven by a button or left unbound is not a whole stick`() {
        val withButton = following(StickSource.LEFT_STICK) + (StickDirection.UP to MappingSource.Button(JoyconButton.X))

        assertNull(withButton.wholeEmittedStick(JoyconSide.DUAL))
        assertNull((following(StickSource.LEFT_STICK) - StickDirection.LEFT).wholeEmittedStick(JoyconSide.DUAL))
    }
}
