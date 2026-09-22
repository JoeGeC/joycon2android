package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun defaultWiimoteMapping(side: JoyconSide) = MappingPresets.default(Console.WIIMOTE_NUNCHUK).entries(side)

private val wiimoteMapping: (PlayerBody) -> Map<String, String> = { defaultWiimoteMapping(it.side) }

private fun wiimoteMappingFor(body: PlayerBody) = defaultWiimoteMapping(body.side)

class DolphinWiimoteConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun merge(existing: String?, players: List<PlayerState>, sidewaysRemote: Boolean = false) =
        DolphinWiimoteConfig.merge(existing, players, { sidewaysRemote }, wiimoteMapping)

    @Test
    fun `right-only player maps the stick to the d-pad and uses no extension`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[Wiimote1]"))
        assertTrue(result.contains("Source = 1"))
        assertTrue(result.contains("Device = DSUClient/0/Joycon2"))
        assertTrue(result.contains("Buttons/A = `Cross`")) // physical A rotates onto B
        assertTrue(result.contains("D-Pad/Up = `Left Y+`"))
        assertTrue(result.contains("IMUIR/Recenter = `R1`"))
        assertTrue(result.contains("Extension = None"))
    }

    @Test
    fun `left-only player maps its directions onto faces and recenters on L`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))))

        assertTrue(result.contains("Buttons/A = `Circle`")) // Down rotates onto A
        assertTrue(result.contains("Buttons/Home = `Touch Button`"))
        assertTrue(result.contains("IMUIR/Recenter = `L1`"))
    }

    @Test
    fun `a pair uses the physical d-pad and exposes the left stick as the nunchuk`() {
        val both = PlayerState(PlayerNumber.P2, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both))

        assertTrue(result.contains("[Wiimote2]"))
        assertTrue(result.contains("Device = DSUClient/1/Joycon2"))
        assertTrue(result.contains("D-Pad/Up = `Pad N`"))
        assertTrue(result.contains("Extension = Nunchuk"))
        assertTrue(result.contains("Nunchuk/Buttons/C = `L1`"))
        assertTrue(result.contains("Nunchuk/Stick/Up = `Left Y+`"))
    }

    @Test
    fun `the nunchuk stick can take its directions from buttons`() {
        val pair = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))
        val mapping = defaultWiimoteMapping(JoyconSide.DUAL) + mapOf("NunchukStick_UP" to "Up")

        val result = DolphinWiimoteConfig.merge(null, listOf(pair), { false }) { mapping }

        assertTrue(result.contains("Nunchuk/Stick/Up = `Pad N`"))
        assertTrue(result.contains("Nunchuk/Stick/Down = `Left Y-`"))
    }

    @Test
    fun `a lone Joy-Con plugs in a nunchuk once its stick is mapped`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("NunchukStick_UP" to "X", "NunchukStick_DOWN" to "B")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { false }) { mapping }

        assertTrue(result.contains("Extension = Nunchuk"))
        assertTrue(result.contains("Nunchuk/Stick/Up = `Circle`")) // physical X rotates onto A
        assertTrue(result.contains("D-Pad/Up = `Left Y+`")) // its own stick still steers the d-pad
        assertFalse(result.contains("Nunchuk/IMUAccelerometer")) // no second hand to stream one
    }

    @Test
    fun `a target bound to several sources fires from any of them`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("DPadUp" to "RIGHT_STICK_UP|SlRight")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { false }) { mapping }

        assertTrue(result.contains("D-Pad/Up = `Left Y+` | `L1`")) // SL rotates onto L held sideways
    }

    @Test
    fun `a pair points the nunchuk accelerometer at the second hand's own slot`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both))

        assertTrue(result.contains("Nunchuk/IMUAccelerometer/Up = `DSUClient/3/Joycon2:Accel Up`"))
        assertTrue(result.contains("Nunchuk/IMUAccelerometer/Backward = `DSUClient/3/Joycon2:Accel Backward`"))
    }

    @Test
    fun `a solo joycon has no nunchuk to give an accelerometer`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertFalse(result.contains("Nunchuk/IMUAccelerometer"))
    }

    @Test
    fun `a pair with no free slot left gets no nunchuk accelerometer`() {
        val players = listOf(
            PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P2, right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P3, right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P4, right = joycon(Side.RIGHT)),
        )

        val result = merge(null, players)

        assertTrue(result.contains("Extension = Nunchuk"))
        assertFalse(result.contains("Nunchuk/IMUAccelerometer"))
    }

    @Test
    fun `a pair drives the swing from the flat grip's thrust axis`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both))

        assertTrue(
            result.contains(
                "Swing/Forward = (`Accel Forward` - `Accel Backward`) - " +
                    "smooth((`Accel Forward` - `Accel Backward`), 0.03)",
            ),
        )
        assertTrue(result.contains("Swing/Forward/Range = 7"))
        assertTrue(result.contains("Swing/Dead Zone = 20"))
    }

    @Test
    fun `a lone Joy-Con thrusts along the axis its nose reads`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("Swing/Forward = (`Accel Right` - `Accel Left`) - smooth("))
    }

    @Test
    fun `the pointer's yaw clamp is widened past a living room's worth`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("IMUIR/Total Yaw = 60"))
    }

    // Off, each Joy-Con is its own body: the nose is the shoulder edge the player aims down.
    @Test
    fun `a right Joy-Con keeps its own body until the layout plays sideways`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up`"))
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Right`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Left`"))
        assertTrue(result.contains("IMUGyroscope/Yaw Left = `Gyro Yaw Left`"))
    }

    // A sideways remote's nose points left, which a left Joy-Con's own body already does.
    @Test
    fun `a left Joy-Con reads the same either way`() {
        val player = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT)))

        listOf(merge(null, player), merge(null, player, sidewaysRemote = true)).forEach { result ->
            assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Left`"))
            assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Right`"))
        }
        // ...though only the sideways one turns its flick into a trick.
        assertFalse(merge(null, player).contains("pulse("))
    }

    @Test
    fun `playing sideways turns a right Joy-Con onto the sideways remote's frame`() {
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = merge(null, player, sidewaysRemote = true)

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up`"))
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Left`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Right`"))
        assertTrue(result.contains("IMUGyroscope/Yaw Left = `Gyro Yaw Left`"))
    }

    // The player's up is a sideways remote's right, so the four bindings turn with the body.
    @Test
    fun `playing sideways turns the d-pad a quarter, on both bodies`() {
        val right = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sidewaysRemote = true)
        val left = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))), sidewaysRemote = true)

        listOf(right, left).forEach { result ->
            assertTrue(result.contains("D-Pad/Right = `Left Y+`")) // the stick's up is the remote's right
            assertTrue(result.contains("D-Pad/Down = `Left X+`"))
            assertTrue(result.contains("D-Pad/Left = `Left Y-`"))
            assertTrue(result.contains("D-Pad/Up = `Left X-`"))
        }
    }

    @Test
    fun `a pair is held like a remote already, so it never turns`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both), sidewaysRemote = true)

        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Forward`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Pitch Up`"))
        assertTrue(result.contains("D-Pad/Up = `Pad N`"))
    }

    // A flick is nearly all rotation, which the game cannot read, so the trick is fired from the
    // gyroscope — and delivered through the accelerometer, the path steering proves reaches the game.
    @Test
    fun `playing sideways turns a wrist flick into a trick`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sidewaysRemote = true)

        val rate = "(`Gyro Pitch Up` + `Gyro Pitch Down` + `Gyro Roll Left` + `Gyro Roll Right` + " +
            "`Gyro Yaw Left` + `Gyro Yaw Right`)"
        val flick = "($rate - smooth($rate, 0.01)) / 5"
        assertTrue(
            result.contains(
                "IMUAccelerometer/Up = `Accel Up` + pulse($flick, 0.6) * " +
                    "sin(timer(0.15) * 6.2832) * 50",
            ),
        )
        assertFalse(result.contains("Shake/")) // Dolphin's own group never landed one
    }

    // Steering a lone Joy-Con held as a wheel is itself rotation, so only what outruns the tracker
    // counts as a flick — otherwise a firm turn shakes the accelerometer the wheel is read from.
    @Test
    fun `a sustained turn is subtracted out of the flick`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sidewaysRemote = true)

        assertTrue(result.contains("- smooth((`Gyro Pitch Up`"))
    }

    // What landed a trick by hand was a hard shake, so opposite inputs swing half a cycle apart
    // rather than one being leaned on.
    @Test
    fun `the trick swings every input, opposites in antiphase`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sidewaysRemote = true)

        listOf("Up", "Left", "Forward").forEach {
            assertTrue(it, result.contains("IMUAccelerometer/$it = `Accel") && result.contains("* 6.2832) * 50"))
        }
        listOf("Down", "Right", "Backward").forEach {
            assertTrue(it, result.contains("* 6.2832 + 3.1416) * 50"))
        }
        assertEquals(6, result.split("pulse(").size - 1)
    }

    // Its remote hand is still while the Nunchuk's stick steers, so a pair can flick for a trick too.
    @Test
    fun `a pair flicks for a trick as well, once the layout plays as a sideways remote`() {
        val pair = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, pair, { true }, ::wiimoteMappingFor)

        assertTrue(result.contains("pulse(((`Gyro Pitch Up`"))

        // ...but its motion frame is untouched, since it is already held like a remote.
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Forward` +"))
    }

    @Test
    fun `a bound source tricks without a flick, so any body can trick at all`() {
        val pair = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))
        val mapping = defaultWiimoteMapping(JoyconSide.DUAL) + mapOf("Shake" to "R")

        val result = DolphinWiimoteConfig.merge(null, pair, { false }) { mapping }

        assertTrue(result.contains("pulse(`R1`, 0.6)"))
    }

    @Test
    fun `nothing shakes the accelerometer when there is no trick to fire`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertFalse(result.contains("pulse("))
    }

    @Test
    fun `pro controllers are skipped`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.PRO))))

        assertFalse(result.contains("[Wiimote1]"))
    }

    @Test
    fun `unrelated sections are preserved and our section is replaced`() {
        val existing = "[Wiimote1]\nButtons/A = `Old`\n[GBA1]\nFoo = Bar\n"

        val result = merge(existing, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[GBA1]"))
        assertTrue(result.contains("Foo = Bar"))
        assertTrue(result.contains("Buttons/A = `Cross`"))
        assertFalse(result.contains("Buttons/A = `Old`"))
    }
}
