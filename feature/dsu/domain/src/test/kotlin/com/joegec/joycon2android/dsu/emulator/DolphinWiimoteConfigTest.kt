package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.defaultMappingEntries
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun defaultWiimoteMapping(side: JoyconSide) = defaultMappingEntries(Console.WIIMOTE_NUNCHUK, side)

class DolphinWiimoteConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun merge(existing: String?, players: List<PlayerState>) =
        DolphinWiimoteConfig.merge(existing, players, ::defaultWiimoteMapping)

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
        assertTrue(result.contains("Buttons/Home = `Touch`"))
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

        val result = DolphinWiimoteConfig.merge(null, listOf(pair)) { mapping }

        assertTrue(result.contains("Nunchuk/Stick/Up = `Pad N`"))
        assertTrue(result.contains("Nunchuk/Stick/Down = `Left Y-`"))
    }

    @Test
    fun `a lone Joy-Con plugs in a nunchuk once its stick is mapped`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("NunchukStick_UP" to "X", "NunchukStick_DOWN" to "B")

        val result = DolphinWiimoteConfig.merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))) { mapping }

        assertTrue(result.contains("Extension = Nunchuk"))
        assertTrue(result.contains("Nunchuk/Stick/Up = `Circle`")) // physical X rotates onto A
        assertTrue(result.contains("D-Pad/Up = `Left Y+`")) // its own stick still steers the d-pad
        assertFalse(result.contains("Nunchuk/IMUAccelerometer")) // no second hand to stream one
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
    fun `a sideways Joy-Con thrusts out through its button face`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("Swing/Forward = (`Accel Up` - `Accel Down`) - smooth("))
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
