package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.BatteryCharge
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The code is the Android keycode, so a moved button breaks every emulator config the app writes. */
class GamepadEventsTest {

    private fun proPlayer(vararg pressed: JoyconButton, stickY: Int = 2048): PlayerState {
        val input = JoyconInput(
            packetId = 0,
            buttons = 0,
            pressed = pressed.map { it.id }.toSet(),
            stickX = 2048,
            stickY = stickY,
            rightStickX = 2048,
            rightStickY = 2048,
            accelX = 0, accelY = 0, accelZ = 0,
            gyroX = 0, gyroY = 0, gyroZ = 0,
            battery = BatteryCharge(100),
        )
        val pro = ConnectedJoycon(address = "pro", side = Side.PRO, deviceName = "Pro", input = input)
        return PlayerState(PlayerNumber.P1, left = pro, right = pro)
    }

    private fun gameCubePlayer(input: JoyconInput): PlayerState {
        val gameCube = ConnectedJoycon(
            address = "gc", side = Side.PRO, model = ControllerModel.GAMECUBE, deviceName = "Joy-Con 2", input = input,
        )
        return PlayerState(PlayerNumber.P1, left = gameCube, right = gameCube)
    }

    private fun events(frame: GamepadFrame) = GamepadEvents.of(frame)

    private fun keysDown(frame: GamepadFrame): Set<Int> =
        events(frame).filter { it.type == Evdev.EV_KEY && it.value == 1 }.map { it.code }.toSet()

    private fun keysDown(vararg pressed: JoyconButton) = keysDown(GamepadFrame(proPlayer(*pressed)))

    private fun axis(frame: GamepadFrame, code: Int): Int =
        events(frame).single { it.type == Evdev.EV_ABS && it.code == code }.value

    @Test
    fun `each button takes the code whose keycode carries its name`() {
        val expected = mapOf(
            JoyconButton.A to 0x130,        // BUTTON_A 96
            JoyconButton.B to 0x131,        // BUTTON_B 97
            JoyconButton.Capture to 0x132,  // BUTTON_C 98
            JoyconButton.X to 0x133,        // BUTTON_X 99
            JoyconButton.Y to 0x134,        // BUTTON_Y 100
            JoyconButton.GL to 0x135,       // BUTTON_Z 101
            JoyconButton.L to 0x136,        // BUTTON_L1 102
            JoyconButton.R to 0x137,        // BUTTON_R1 103
            JoyconButton.ZL to 0x138,       // BUTTON_L2 104
            JoyconButton.ZR to 0x139,       // BUTTON_R2 105
            JoyconButton.Minus to 0x13A,    // BUTTON_SELECT 109
            JoyconButton.Plus to 0x13B,     // BUTTON_START 108
            JoyconButton.Home to 0x13C,     // BUTTON_MODE 110
            JoyconButton.LS to 0x13D,       // BUTTON_THUMBL 106
            JoyconButton.RS to 0x13E,       // BUTTON_THUMBR 107
        )
        expected.forEach { (button, code) -> assertEquals(button.name, setOf(code), keysDown(button)) }
    }

    @Test
    fun `the two buttons past the gamepad range take BTN_0 and BTN_1`() {
        assertEquals(setOf(0x100), keysDown(JoyconButton.GR))   // BUTTON_1 188
        assertEquals(setOf(0x101), keysDown(JoyconButton.Chat)) // BUTTON_2 189
    }

    @Test
    fun `ZL drives the brake and ZR the gas, matching Android's trigger aliases`() {
        val zl = GamepadFrame(proPlayer(JoyconButton.ZL))
        val zr = GamepadFrame(proPlayer(JoyconButton.ZR))

        assertEquals(255, axis(zl, Evdev.ABS_BRAKE))
        assertEquals(0, axis(zl, Evdev.ABS_GAS))
        assertEquals(255, axis(zr, Evdev.ABS_GAS))
        assertEquals(0, axis(zr, Evdev.ABS_BRAKE))
    }

    @Test
    fun `the d-pad is the hat, up negative`() {
        val upRight = GamepadFrame(proPlayer(JoyconButton.Up, JoyconButton.Right))

        assertEquals(1, axis(upRight, Evdev.ABS_HAT0X))
        assertEquals(-1, axis(upRight, Evdev.ABS_HAT0Y))
    }

    @Test
    fun `a centred stick sits at zero and full up is negative`() {
        val centred = GamepadFrame(proPlayer())
        val up = GamepadFrame(proPlayer(stickY = 4095))

        assertEquals(0, axis(centred, Evdev.ABS_X))
        assertEquals(-32751, axis(up, Evdev.ABS_Y))
    }

    @Test
    fun `the frame's Home and Back take KEY_HOMEPAGE and KEY_BACK`() {
        val frame = GamepadFrame(proPlayer(), setOf(AndroidKey.HOME, AndroidKey.BACK))

        assertEquals(setOf(172, 158), keysDown(frame))
    }

    @Test
    fun `every key the pad can send has a code`() {
        assertEquals(GamepadFrame.SENDABLE_KEYS, GamepadEvents.ANDROID_KEY_CODES.keys)
    }

    @Test
    fun `with no Android keys Home stays a gamepad button`() {
        assertEquals(setOf(0x13C), keysDown(JoyconButton.Home)) // BUTTON_MODE
    }

    @Test
    fun `every event is one the device declares`() {
        val device = GamepadDescriptor.device(1)
        val declared = device.keys.map { Evdev.EV_KEY to it } + device.axes.map { Evdev.EV_ABS to it.code }

        assertEquals(declared.toSet(), events(GamepadFrame(proPlayer())).map { it.type to it.code }.toSet())
    }

    @Test
    fun `a gamecube controller's analog travel drives the triggers, not ZL or ZR`() {
        val frame = GamepadFrame(gameCubePlayer(JoyconInput(pressed = setOf(JoyconButton.ZL.id), triggerLeft = 128, triggerRight = 40)))

        assertEquals(128, axis(frame, Evdev.ABS_BRAKE))
        assertEquals(40, axis(frame, Evdev.ABS_GAS))
    }

    @Test
    fun `a gamecube controller's ZL and Z travel on the bumpers, clear of L2 and R2`() {
        val frame = GamepadFrame(gameCubePlayer(JoyconInput(pressed = setOf(JoyconButton.ZL.id, JoyconButton.ZR.id))))

        assertEquals(setOf(0x136, 0x137), keysDown(frame)) // BUTTON_L1, BUTTON_R1
    }

    @Test
    fun `a gamecube controller's trigger clicks travel on the stick clicks`() {
        val frame = GamepadFrame(gameCubePlayer(JoyconInput(pressed = setOf(JoyconButton.L.id, JoyconButton.R.id))))

        assertTrue(keysDown(frame).containsAll(setOf(0x13D, 0x13E))) // BUTTON_THUMBL, BUTTON_THUMBR
    }
}
