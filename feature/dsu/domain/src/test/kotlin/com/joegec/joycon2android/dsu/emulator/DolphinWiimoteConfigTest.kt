package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.buttonmapping.preset.ClassicControllerMapping
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun defaultWiimoteMapping(side: JoyconSide) = MappingPresets.default(Console.WIIMOTE_NUNCHUK).entries(side)

private val wiimoteMapping: (PlayerBody) -> Map<String, String> = { defaultWiimoteMapping(it.side) }

private val unscaled = RemoteMotion(sensitivityPercent = RemoteMotion.UNSCALED_SENSITIVITY_PERCENT)

class DolphinWiimoteConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun merge(
        existing: String?,
        players: List<PlayerState>,
        controller: WiiController = WiiController.REMOTE,
        thrust: Boolean = false,
        sensitivityPercent: Int = RemoteMotion.UNSCALED_SENSITIVITY_PERCENT,
    ) = DolphinWiimoteConfig.merge(
        existing,
        players,
        { RemoteMotion(controller, thrust, sensitivityPercent) },
        wiimoteMapping,
    )

    @Test
    fun `right-only player maps the stick to the d-pad and uses no extension`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[Wiimote1]"))
        assertTrue(result.contains("Source = 1"))
        assertTrue(result.contains("Device = DSUClient/0/Joycon2"))
        assertTrue(result.contains("Buttons/A = `Cross`")) // physical A rotates onto B
        assertTrue(result.contains("D-Pad/Up = `Left X+`"))
        assertTrue(result.contains("IMUIR/Recenter = `R1`"))
        assertTrue(result.contains("Extension = None"))
    }

    @Test
    fun `left-only player maps its directions onto faces and recenters on L`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))))

        assertTrue(result.contains("Buttons/A = `Triangle`")) // Right rotates onto X
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

        val result = DolphinWiimoteConfig.merge(null, listOf(pair), { unscaled }) { mapping }

        assertTrue(result.contains("Nunchuk/Stick/Up = `Pad N`"))
        assertTrue(result.contains("Nunchuk/Stick/Down = `Left Y-`"))
    }

    @Test
    fun `a lone Joy-Con plugs in a nunchuk once its stick is mapped`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("NunchukStick_UP" to "X", "NunchukStick_DOWN" to "B")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { unscaled }) { mapping }

        assertTrue(result.contains("Extension = Nunchuk"))
        assertTrue(result.contains("Nunchuk/Stick/Up = `Circle`")) // physical X rotates onto A
        assertTrue(result.contains("D-Pad/Up = `Left X+`")) // its own stick still steers the d-pad
        assertFalse(result.contains("Nunchuk/IMUAccelerometer")) // no second hand to stream one
    }

    @Test
    fun `recenter follows its mapping`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("Recenter" to "Plus")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { unscaled }) { mapping }

        assertTrue(result.contains("IMUIR/Recenter = `Options`"))
    }

    @Test
    fun `an unbound recenter writes no binding`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("Recenter" to "")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { unscaled }) { mapping }

        assertFalse(result.contains("IMUIR/Recenter ="))
    }

    @Test
    fun `a target bound to several sources fires from any of them`() {
        val mapping = defaultWiimoteMapping(JoyconSide.RIGHT) + mapOf("DPadUp" to "RIGHT_STICK_UP|SlRight")
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = DolphinWiimoteConfig.merge(null, player, { unscaled }) { mapping }

        assertTrue(result.contains("D-Pad/Up = `Left X+` | `L1`")) // SL rotates onto L held sideways
    }

    @Test
    fun `a remote left on our server by an earlier setup is disconnected, so a pair's second hand isn't a player`() {
        val earlier = "[Wiimote4]\nSource = 1\nDevice = DSUClient/3/Joycon2\n"
        val pair = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(earlier, listOf(pair))

        assertEquals("0", IniEditor.valueOf(result, "[Wiimote4]", "Source"))
        assertEquals(null, IniEditor.valueOf(result, "[Wiimote4]", "Device"))
    }

    @Test
    fun `a remote the player set up themselves is left alone`() {
        val theirs = "[Wiimote2]\nSource = 2\n[Wiimote3]\nSource = 1\nDevice = Android/0/Pro Controller\n"

        val result = merge(theirs, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertEquals("2", IniEditor.valueOf(result, "[Wiimote2]", "Source"))
        assertEquals("1", IniEditor.valueOf(result, "[Wiimote3]", "Source"))
        assertEquals("Android/0/Pro Controller", IniEditor.valueOf(result, "[Wiimote3]", "Device"))
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

        val result = merge(null, listOf(both), thrust = true)

        val thrust = "(`Accel Forward` - `Accel Backward`)"
        assertTrue(result.contains("Swing/Forward = ${DolphinHighPass.of(thrust, "Swing/Forward")}\n"))
        assertTrue(result.contains("Swing/Forward/Range = 7"))
        assertTrue(result.contains("Swing/Dead Zone = 20"))
    }

    @Test
    fun `a lone Joy-Con thrusts along the axis its nose reads`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), thrust = true)

        assertTrue(result.contains("Swing/Forward = (\$Swing_Forward_now = timer(1000), "))
        assertTrue(result.contains("\$Swing_Forward_slew = \$Swing_Forward_slew + clamp((`Accel Right` - `Accel Left`) - "))
    }

    @Test
    fun `sensitivity boosts each accelerometer axis by its high-passed motion`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both), sensitivityPercent = 125)

        val up = "(`Accel Up` - `Accel Down`)"
        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up` + 0.25 * ${DolphinHighPass.of(up, "IMUAccelerometer/Up")}\n"))
        assertTrue(result.contains("IMUAccelerometer/Down = `Accel Down`\n"))
        assertTrue(result.contains("IMUAccelerometer/Left = `Accel Left` + 0.25 * ("))
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Forward` + 0.25 * ("))
    }

    @Test
    fun `sensitivity boosts the nunchuk's accelerometer from the second hand's slot`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both), sensitivityPercent = 125)

        val up = "(`DSUClient/3/Joycon2:Accel Up` - `DSUClient/3/Joycon2:Accel Down`)"
        val highPass = DolphinHighPass.of(up, "Nunchuk/IMUAccelerometer/Up")
        assertTrue(result.contains("Nunchuk/IMUAccelerometer/Up = `DSUClient/3/Joycon2:Accel Up` + 0.25 * $highPass\n"))
        assertTrue(result.contains("Nunchuk/IMUAccelerometer/Down = `DSUClient/3/Joycon2:Accel Down`\n"))
    }

    @Test
    fun `a sensitivity under 100 takes motion away`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sensitivityPercent = 80)

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up` - 0.2 * ("))
    }

    @Test
    fun `a lone Joy-Con's boost follows its body's axes`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), sensitivityPercent = 150)

        assertTrue(result.contains("IMUAccelerometer/Left = `Accel Forward` + 0.5 * (\$IMUAccelerometer_Left_now"))
        assertTrue(result.contains("clamp((`Accel Forward` - `Accel Backward`) - \$IMUAccelerometer_Left_slew, "))
    }

    @Test
    fun `no line uses Dolphin's smooth, which can't settle on FEAT_AFP phones`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both), thrust = true, sensitivityPercent = 150)

        assertFalse(result.contains("smooth("))
    }

    @Test
    fun `each high-passed control keeps its own slew`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = merge(null, listOf(both), thrust = true, sensitivityPercent = 150)

        val slews = Regex("""\$([A-Za-z_]+)_slew = """).findAll(result).map { it.groupValues[1] }.toList()
        assertEquals(7, slews.size) // 3 remote axes, 3 nunchuk axes, swing
        assertEquals(slews.size, slews.toSet().size)
    }

    @Test
    fun `at 100 percent the accelerometer reads straight through`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertFalse(result.contains("IMUAccelerometer/Up = `Accel Up` +"))
    }

    @Test
    fun `without thrusts the swing is left unbound, so the game reads the accelerometer alone`() {
        val earlier = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), thrust = true)

        val result = merge(earlier, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertFalse(result.contains("Swing/"))
    }

    @Test
    fun `the pointer's yaw clamp is widened past a living room's worth`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("IMUIR/Total Yaw = 60"))
    }

    @Test
    fun `a right Joy-Con keeps its own body until the layout plays sideways`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up`"))
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Right`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Left`"))
        assertTrue(result.contains("IMUGyroscope/Yaw Left = `Gyro Yaw Left`"))
    }

    @Test
    fun `a left Joy-Con reads the same either way`() {
        val player = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT)))

        listOf(merge(null, player), merge(null, player, controller = WiiController.SIDEWAYS_REMOTE)).forEach { result ->
            assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Left`"))
            assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Right`"))
        }
        // ...though only the sideways one turns its flick into a trick.
        assertFalse(merge(null, player).contains("pulse("))
    }

    @Test
    fun `playing sideways turns a right Joy-Con onto the sideways remote's frame`() {
        val player = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = merge(null, player, controller = WiiController.SIDEWAYS_REMOTE)

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up`"))
        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Left`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Right`"))
        assertTrue(result.contains("IMUGyroscope/Yaw Left = `Gyro Yaw Left`"))
    }

    @Test
    fun `off a sideways remote, a lone Joy-Con's stick points up toward its L or R`() {
        val right = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))
        val left = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))))

        assertTrue(right.contains("D-Pad/Up = `Left X+`"))
        assertTrue(right.contains("D-Pad/Left = `Left Y+`")) // the rail
        assertTrue(left.contains("D-Pad/Up = `Left X-`"))
        assertTrue(left.contains("D-Pad/Right = `Left Y+`")) // the rail
    }

    @Test
    fun `playing sideways turns the d-pad a quarter, on both bodies`() {
        val right = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), controller = WiiController.SIDEWAYS_REMOTE)
        val left = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))), controller = WiiController.SIDEWAYS_REMOTE)

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

        val result = merge(null, listOf(both), controller = WiiController.SIDEWAYS_REMOTE)

        assertTrue(result.contains("IMUAccelerometer/Forward = `Accel Forward`"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Pitch Up`"))
        assertTrue(result.contains("D-Pad/Up = `Pad N`"))
    }

    @Test
    fun `playing sideways turns a wrist flick into a jerk the way it was flicked`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), controller = WiiController.SIDEWAYS_REMOTE)

        val up = "(`Gyro Pitch Up` / 9) & not(pulse(`Gyro Pitch Down` / 9, 0.4))"
        assertTrue(
            result.contains(
                "IMUAccelerometer/Up = `Accel Up` + pulse($up, 0.6) * " +
                    "max(sin(timer(0.15) * 6.2832), 0) * 50",
            ),
        )
        assertFalse(result.contains("Shake/")) // Dolphin's own group never landed one
    }

    @Test
    fun `each flick direction locks the other out`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), controller = WiiController.SIDEWAYS_REMOTE)

        assertTrue(result.contains("IMUAccelerometer/Down = `Accel Down` + pulse((`Gyro Pitch Down` / 9) & " +
            "not(pulse(`Gyro Pitch Up` / 9, 0.4))"))
    }

    @Test
    fun `the jerks all go the way the flick did`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))), controller = WiiController.SIDEWAYS_REMOTE)

        assertEquals(2, result.split("pulse((`Gyro").size - 1) // one per direction, nothing sideways
        assertFalse(result.contains("3.1416")) // no antiphase left to cancel it
    }

    @Test
    fun `a bound source jerks without a flick, so any body can trick at all`() {
        val pair = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))
        val mapping = defaultWiimoteMapping(JoyconSide.DUAL) + mapOf("Shake" to "R")

        val result = DolphinWiimoteConfig.merge(null, pair, { unscaled }) { mapping }

        assertTrue(result.contains("IMUAccelerometer/Up = `Accel Up` + pulse(`R1`, 0.6)"))
        assertFalse(result.contains("IMUAccelerometer/Down = `Accel Down` + pulse")) // a button has no direction
    }

    @Test
    fun `nothing jerks the accelerometer when there is no flick to fire`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertFalse(result.contains("pulse("))
    }

    @Test
    fun `pro controllers are skipped`() {
        val result = merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.PRO))))

        assertFalse(result.contains("[Wiimote1]"))
    }

    private val classic = unscaled.copy(controller = WiiController.CLASSIC)

    private fun mergeClassic(players: List<PlayerState>, motion: RemoteMotion = classic) = DolphinWiimoteConfig.merge(
        null,
        players,
        { motion },
        { ClassicControllerMapping.entries(it.side) },
    )

    @Test
    fun `a Classic Controller replaces the nunchuk and binds no remote buttons`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = mergeClassic(listOf(both))

        assertTrue(result.contains("Extension = Classic"))
        assertTrue(result.contains("Classic/Buttons/A = `Circle`"))
        assertTrue(result.contains("Classic/Triggers/L = `L1`"))
        assertTrue(result.contains("Classic/Buttons/ZL = `L2`"))
        assertTrue(result.contains("Classic/Buttons/Home = `PS`"))
        assertTrue(result.contains("Classic/D-Pad/Up = `Pad N`"))
        assertTrue(result.contains("Classic/Left Stick/Up = `Left Y+`"))
        assertTrue(result.contains("Classic/Right Stick/Left = `Right X-`"))
        assertFalse(result.contains("Buttons/A = `Cross`"))
        assertFalse(result.contains("Nunchuk/"))
    }

    @Test
    fun `the remote a Classic Controller plugs into still points, but never thrusts or flicks`() {
        val right = PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))

        val result = mergeClassic(listOf(right), classic.copy(thrust = true))

        assertTrue(result.contains("IMUIR/Enabled = True"))
        assertTrue(result.contains("IMUGyroscope/Pitch Up = `Gyro Roll Right`")) // held sideways
        assertFalse(result.contains("Swing/"))
        assertFalse(result.contains("pulse("))
    }

    @Test
    fun `a GameCube controller's Classic Controller has nothing to point with`() {
        val gameCube = ConnectedJoycon(address = "GC", side = Side.PRO, model = ControllerModel.GAMECUBE, deviceName = "GameCube")

        val result = mergeClassic(listOf(PlayerState(PlayerNumber.P1, left = gameCube)))

        assertFalse(result.contains("IMU"))
    }

    @Test
    fun `a pro controller is set up once it plays a Classic Controller`() {
        val pro = PlayerState(PlayerNumber.P1, left = joycon(Side.PRO))

        val result = mergeClassic(listOf(pro))

        assertTrue(result.contains("[Wiimote1]"))
        assertTrue(result.contains("Device = DSUClient/0/Joycon2"))
        assertTrue(result.contains("Classic/Buttons/B = `Cross`"))
        assertTrue(DolphinWiimoteConfig.writesFor(pro) { classic })
        assertFalse(DolphinWiimoteConfig.writesFor(pro) { unscaled })
    }

    @Test
    fun `a GameCube controller plays the Classic Controller's minus on Capture`() {
        val gameCube = ConnectedJoycon(address = "GC", side = Side.PRO, model = ControllerModel.GAMECUBE, deviceName = "GameCube")

        val result = mergeClassic(listOf(PlayerState(PlayerNumber.P1, left = gameCube)))

        assertTrue(result.contains("Classic/Buttons/- = `Touch Button`"))
        assertTrue(result.contains("Classic/Buttons/A = `Circle`"))
    }

    @Test
    fun `a lone Joy-Con plays the Classic Controller sideways, with no d-pad`() {
        val result = mergeClassic(listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("Classic/Buttons/A = `Circle`")) // X rotates onto A
        assertFalse(result.contains("Classic/D-Pad/"))
        assertTrue(result.contains("Classic/Left Stick/Up = `Left Y+`"))
        assertFalse(result.contains("Classic/Right Stick/"))
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
