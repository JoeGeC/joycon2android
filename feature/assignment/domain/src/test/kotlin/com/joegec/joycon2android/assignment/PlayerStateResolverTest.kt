package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PlayerStateResolverTest {

    private val evicted = mutableListOf<String>()
    private val resolver = PlayerStateResolver(evictConflicting = { evicted.add(it) })

    @Test
    fun `lone left joycon fills the left slot`() {
        val left = joycon("AA", Side.LEFT)

        val state = resolver.resolve(PlayerNumber.P1, listOf(left))

        assertSame(left, state.left)
        assertNull(state.right)
    }

    @Test
    fun `lone right joycon fills the right slot`() {
        val right = joycon("BB", Side.RIGHT)

        val state = resolver.resolve(PlayerNumber.P1, listOf(right))

        assertSame(right, state.right)
        assertNull(state.left)
    }

    @Test
    fun `undetermined lone joycon defaults to the left slot, not right`() {
        val unknown = joycon("CC", Side.UNKNOWN)

        val state = resolver.resolve(PlayerNumber.P1, listOf(unknown))

        assertSame(unknown, state.left)
        assertNull(state.right)
    }

    @Test
    fun `undetermined lone joycon is inferred left from a left-exclusive button`() {
        val unknown = joycon("CC", Side.UNKNOWN, JoyconButton.L)

        val state = resolver.resolve(PlayerNumber.P1, listOf(unknown))

        assertSame(unknown, state.left)
        assertNull(state.right)
    }

    @Test
    fun `undetermined lone joycon is inferred right from a right-exclusive button`() {
        val unknown = joycon("CC", Side.UNKNOWN, JoyconButton.A)

        val state = resolver.resolve(PlayerNumber.P1, listOf(unknown))

        assertSame(unknown, state.right)
        assertNull(state.left)
    }

    @Test
    fun `a pro controller fills both slots`() {
        val pro = joycon("DD", Side.PRO)

        val state = resolver.resolve(PlayerNumber.P1, listOf(pro))

        assertSame(pro, state.left)
        assertSame(pro, state.right)
    }

    @Test
    fun `a known left and known right pair into their slots`() {
        val left = joycon("AA", Side.LEFT)
        val right = joycon("BB", Side.RIGHT)

        val state = resolver.resolve(PlayerNumber.P1, listOf(left, right))

        assertSame(left, state.left)
        assertSame(right, state.right)
    }

    private fun joycon(address: String, side: Side, vararg held: JoyconButton) = ConnectedJoycon(
        address = address,
        side = side,
        deviceName = "Joy-Con",
        input = JoyconInput(pressed = held.map { it.id }.toSet()),
        ready = true,
    )
}
