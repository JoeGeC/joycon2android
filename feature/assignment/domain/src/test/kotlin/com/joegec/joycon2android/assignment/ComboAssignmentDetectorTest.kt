package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComboAssignmentDetectorTest {

    private val detector = ComboAssignmentDetector()

    @Test
    fun `L on left joycon and R on right joycon pairs both onto one combo`() {
        val left = joycon("AA", Side.LEFT, JoyconButton.L)
        val right = joycon("BB", Side.RIGHT, JoyconButton.R)

        val combos = detector.detect(listOf(left, right))

        assertEquals(listOf(ComboAssignment(listOf("AA", "BB"))), combos)
    }

    @Test
    fun `L alone does not trigger`() {
        val combos = detector.detect(listOf(joycon("AA", Side.LEFT, JoyconButton.L)))

        assertTrue(combos.isEmpty())
    }

    @Test
    fun `SL+SR on a single joycon assigns it solo`() {
        val sideways = joycon("AA", Side.LEFT, JoyconButton.SlLeft, JoyconButton.SrLeft)

        val combos = detector.detect(listOf(sideways))

        assertEquals(listOf(ComboAssignment(listOf("AA"))), combos)
    }

    @Test
    fun `SL+SR works on right-rail buttons too`() {
        val sideways = joycon("AA", Side.RIGHT, JoyconButton.SlRight, JoyconButton.SrRight)

        val combos = detector.detect(listOf(sideways))

        assertEquals(listOf(ComboAssignment(listOf("AA"))), combos)
    }

    @Test
    fun `SL on one joycon and SR on another does not trigger`() {
        val first = joycon("AA", Side.LEFT, JoyconButton.SlLeft)
        val second = joycon("BB", Side.LEFT, JoyconButton.SrLeft)

        val combos = detector.detect(listOf(first, second))

        assertTrue(combos.isEmpty())
    }

    @Test
    fun `L+R on a pro controller assigns it solo`() {
        val pro = joycon("AA", Side.PRO, JoyconButton.L, JoyconButton.R)

        val combos = detector.detect(listOf(pro))

        assertEquals(listOf(ComboAssignment(listOf("AA"))), combos)
    }

    @Test
    fun `pro controller does not pair with a joycon`() {
        val pro = joycon("AA", Side.PRO, JoyconButton.L)
        val right = joycon("BB", Side.RIGHT, JoyconButton.R)

        val combos = detector.detect(listOf(pro, right))

        assertTrue(combos.isEmpty())
    }

    @Test
    fun `held combo fires only once`() {
        val pro = joycon("AA", Side.PRO, JoyconButton.L, JoyconButton.R)

        detector.detect(listOf(pro))
        val secondPass = detector.detect(listOf(pro))

        assertTrue(secondPass.isEmpty())
    }

    @Test
    fun `combo can fire again after all combo buttons are released`() {
        val pro = joycon("AA", Side.PRO, JoyconButton.L, JoyconButton.R)

        detector.detect(listOf(pro))
        detector.detect(listOf(joycon("AA", Side.PRO)))
        val rePress = detector.detect(listOf(pro))

        assertEquals(listOf(ComboAssignment(listOf("AA"))), rePress)
    }

    @Test
    fun `controllers that are not ready are ignored`() {
        val pro = joycon("AA", Side.PRO, JoyconButton.L, JoyconButton.R).copy(ready = false)

        val combos = detector.detect(listOf(pro))

        assertTrue(combos.isEmpty())
    }

    @Test
    fun `two pairs assign independently`() {
        val combos = detector.detect(
            listOf(
                joycon("AA", Side.LEFT, JoyconButton.L),
                joycon("BB", Side.RIGHT, JoyconButton.R),
                joycon("CC", Side.LEFT, JoyconButton.L),
                joycon("DD", Side.RIGHT, JoyconButton.R),
            )
        )

        assertEquals(2, combos.size)
        assertEquals(setOf("AA", "BB", "CC", "DD"), combos.flatMap { it.addresses }.toSet())
    }

    @Test
    fun `unknown-side joycons pair via L and R`() {
        val left = joycon("AA", Side.UNKNOWN, JoyconButton.L)
        val right = joycon("BB", Side.UNKNOWN, JoyconButton.R)

        val combos = detector.detect(listOf(left, right))

        assertEquals(listOf(ComboAssignment(listOf("AA", "BB"))), combos)
    }

    private fun joycon(address: String, side: Side, vararg held: JoyconButton) = ConnectedJoycon(
        address = address,
        side = side,
        deviceName = "Joy-Con",
        input = JoyconInput(pressed = held.map { it.id }.toSet()),
        ready = true,
    )
}
