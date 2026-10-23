package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.MouseSensor
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JoyconMouseTest {

    private val mouse = JoyconMouse(Side.LEFT)

    private fun input(
        x: Int = 0,
        y: Int = 0,
        distance: Int = 150,
        packetId: Int = 0,
        stickY: Int = 2048,
        vararg pressed: JoyconButton,
        accelX: Int = RAIL_DOWN_LEFT,
    ) = JoyconInput(
        packetId = packetId,
        accelX = accelX,
        pressed = pressed.map { it.id }.toSet(),
        stickY = stickY,
        mouse = MouseSensor(x, y, distance),
    )

    @Test
    fun `the first report on a surface moves nothing`() {
        assertEquals(MouseFrame(), mouse.read(input(x = 500, y = 900)))
    }

    @Test
    fun `movement is the difference between reports`() {
        mouse.read(input(x = 100, y = 100))

        assertEquals(MouseFrame(dx = 40, dy = -25), mouse.read(input(x = 140, y = 75)))
    }

    @Test
    fun `movement carries across the int16 wrap`() {
        mouse.read(input(x = 32760))

        assertEquals(10, mouse.read(input(x = -32766))!!.dx)
    }

    @Test
    fun `a lifted joy-con is no mouse and starts afresh when set down`() {
        mouse.read(input(x = 100))

        assertNull(mouse.read(input(x = 100, distance = 3100)))
        assertEquals(0, mouse.read(input(x = 900))!!.dx)
    }

    @Test
    fun `a controller without the sensor is no mouse`() {
        assertNull(mouse.read(JoyconInput()))
    }

    @Test
    fun `either side's shoulder clicks and its trigger right-clicks`() {
        assertEquals(setOf(MouseButton.PRIMARY), mouse.read(input(pressed = arrayOf(JoyconButton.R)))!!.buttons)
        assertEquals(setOf(MouseButton.PRIMARY), mouse.read(input(pressed = arrayOf(JoyconButton.L)))!!.buttons)
        assertEquals(setOf(MouseButton.SECONDARY), mouse.read(input(pressed = arrayOf(JoyconButton.ZR)))!!.buttons)
        assertEquals(setOf(MouseButton.SECONDARY), mouse.read(input(pressed = arrayOf(JoyconButton.ZL)))!!.buttons)
    }

    @Test
    fun `a full stick tilt scrolls 20 detents a second`() {
        val ticks = (0..1000 step 10).sumOf { ms -> mouse.read(input(packetId = ms, stickY = 4095))!!.wheel }

        assertEquals(20, ticks)
    }

    @Test
    fun `stick down scrolls down`() {
        mouse.read(input(packetId = 0, stickY = 0))

        assertEquals(-2, mouse.read(input(packetId = 100, stickY = 0))!!.wheel)
    }

    @Test
    fun `a stick inside the dead zone never scrolls`() {
        mouse.read(input(packetId = 0, stickY = 2300))

        assertEquals(0, mouse.read(input(packetId = 100, stickY = 2300))!!.wheel)
    }

    @Test
    fun `a repeated packet adds no scroll`() {
        mouse.read(input(packetId = 0, stickY = 4095))
        mouse.read(input(packetId = 100, stickY = 4095))

        assertEquals(0, mouse.read(input(packetId = 100, stickY = 4095))!!.wheel)
    }

    @Test
    fun `the clicks and stick are withheld from the game`() {
        val withheld = JoyconMouse.withheldFrom(
            input(stickY = 4095, pressed = arrayOf(JoyconButton.R, JoyconButton.ZR, JoyconButton.A)),
        )

        assertEquals(setOf(JoyconButton.A.id), withheld.pressed)
        assertEquals(2048, withheld.stickY)
    }

    @Test
    fun `a Joy-Con held in a grip is no mouse, even with fingers over the sensor`() {
        assertNull(mouse.read(input(x = 100, accelX = 0)))
    }

    @Test
    fun `a sideways Joy-Con's rail faces up, so it is no mouse either`() {
        assertNull(mouse.read(input(x = 100, accelX = -RAIL_DOWN_LEFT)))
    }

    @Test
    fun `a right Joy-Con stands on its rail with gravity the other way`() {
        val right = JoyconMouse(Side.RIGHT)

        assertEquals(MouseFrame(), right.read(input(accelX = -RAIL_DOWN_LEFT)))
        assertNull(right.read(input(accelX = RAIL_DOWN_LEFT)))
    }

    @Test
    fun `without its default controls it only moves the pointer`() {
        mouse.read(input(x = 100, packetId = 0, stickY = 4095))

        val frame = mouse.read(input(x = 120, packetId = 100, stickY = 4095, pressed = arrayOf(JoyconButton.R)), defaultControls = false)

        assertEquals(MouseFrame(dx = 20), frame)
    }

    private companion object {
        const val RAIL_DOWN_LEFT = -4096
    }
}
