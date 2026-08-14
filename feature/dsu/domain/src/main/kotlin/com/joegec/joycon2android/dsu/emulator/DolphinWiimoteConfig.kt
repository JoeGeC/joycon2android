package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.buttonmapping.toButtonMap
import com.joegec.joycon2android.buttonmapping.toStickMap
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.SidewaysMapper

/**
 * Generates Dolphin's WiimoteNew.ini button mappings for the DSU device, one `[WiimoteN]`
 * section per assigned player (player N streams on DSU slot N-1 → `DSUClient/<slot>/Joycon2`,
 * the name matching our [DolphinDsuConfig] entry), driven by the user's customizable Joy-Con ->
 * Wiimote/Nunchuk mapping. A single sideways Joy-Con drives the D-pad target from its own analog
 * stick (there's only one, so it isn't user-routable); a pair drives the D-pad target from the
 * physical D-pad and exposes a routable stick as the Nunchuk. [DS4_NAMES]/[PAD_NAMES] are the
 * fixed, body-independent DS4-convention names the DSU device exposes for each Android
 * input (see the in-app mapping table); [specFor] applies the same physical -> virtual remap
 * [SidewaysMapper] uses for live input to find the right one for a customized source.
 */
object DolphinWiimoteConfig {
    val path = DolphinPaths.config("WiimoteNew.ini")

    private val DOLPHIN_KEYS = mapOf(
        WiimoteButton.A to "Buttons/A",
        WiimoteButton.B to "Buttons/B",
        WiimoteButton.One to "Buttons/1",
        WiimoteButton.Two to "Buttons/2",
        WiimoteButton.Home to "Buttons/Home",
        WiimoteButton.Plus to "Buttons/+",
        WiimoteButton.Minus to "Buttons/-",
        WiimoteButton.DPadUp to "D-Pad/Up",
        WiimoteButton.DPadDown to "D-Pad/Down",
        WiimoteButton.DPadLeft to "D-Pad/Left",
        WiimoteButton.DPadRight to "D-Pad/Right",
        WiimoteButton.NunchukC to "Nunchuk/Buttons/C",
        WiimoteButton.NunchukZ to "Nunchuk/Buttons/Z",
    )

    // DSU carries exactly the DS4 button set — these are protocol input names, not UI copy.
    private val DS4_NAMES = mapOf(
        JoyconButton.A to "Circle",
        JoyconButton.B to "Cross",
        JoyconButton.X to "Triangle",
        JoyconButton.Y to "Square",
        JoyconButton.L to "L1",
        JoyconButton.R to "R1",
        JoyconButton.ZL to "L2",
        JoyconButton.ZR to "R2",
        JoyconButton.Minus to "Share",
        JoyconButton.Plus to "Options",
        JoyconButton.LS to "L3",
        JoyconButton.RS to "R3",
        JoyconButton.Home to "PS",
        JoyconButton.Camera to "Touch",
    )

    // The D-Pad target reads the DS4 pad nibble instead of a face button.
    private val PAD_NAMES = mapOf(
        JoyconButton.Up to "Pad N",
        JoyconButton.Down to "Pad S",
        JoyconButton.Left to "Pad W",
        JoyconButton.Right to "Pad E",
    )

    private val imuLines = listOf(
        "IMUAccelerometer/Up = `Accel Up`",
        "IMUAccelerometer/Down = `Accel Down`",
        "IMUAccelerometer/Left = `Accel Left`",
        "IMUAccelerometer/Right = `Accel Right`",
        "IMUAccelerometer/Forward = `Accel Forward`",
        "IMUAccelerometer/Backward = `Accel Backward`",
        "IMUGyroscope/Pitch Up = `Gyro Pitch Up`",
        "IMUGyroscope/Pitch Down = `Gyro Pitch Down`",
        "IMUGyroscope/Roll Left = `Gyro Roll Left`",
        "IMUGyroscope/Roll Right = `Gyro Roll Right`",
        "IMUGyroscope/Yaw Left = `Gyro Yaw Left`",
        "IMUGyroscope/Yaw Right = `Gyro Yaw Right`",
        "IMUIR/Enabled = True",
    )

    // A sideways single Joy-Con's own stick isn't user-routable — there's only one.
    private val nativeStickDPad = listOf(
        "D-Pad/Up = `Left Y+`",
        "D-Pad/Down = `Left Y-`",
        "D-Pad/Left = `Left X-`",
        "D-Pad/Right = `Left X+`",
    )

    fun merge(existing: String?, players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): String =
        IniEditor.mergeSections(existing, sections(players, mappingFor))

    private fun sections(players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): Map<String, String> =
        players.mapNotNull { player ->
            val slot = player.player.index - 1
            if (slot !in 0..3) return@mapNotNull null
            bodyFor(player, slot, mappingFor)?.let { "[Wiimote${player.player.index}]" to it }
        }.toMap()

    private fun bodyFor(player: PlayerState, slot: Int, mappingFor: (JoyconSide) -> Map<String, String>): String? {
        val side = when {
            player.hasPro -> return null
            player.hasFullController -> JoyconSide.DUAL
            player.right != null -> JoyconSide.RIGHT
            player.left != null -> JoyconSide.LEFT
            else -> return null
        }
        // Source = 1 forces this Wii Remote slot to Emulated, so the mappings actually apply
        val header = listOf("Source = 1", "Device = DSUClient/$slot/Joycon2")
        return (header + lines(side, mappingFor(side)) + imuLines).joinToString("\n", postfix = "\n")
    }

    private fun lines(side: JoyconSide, mapping: Map<String, String>): List<String> {
        val buttonLines = mapping.toButtonMap<WiimoteButton>().mapNotNull { (target, source) ->
            specFor(side, source)?.let { spec -> "${DOLPHIN_KEYS.getValue(target)} = `$spec`" }
        }
        val recenterSpec = if (side == JoyconSide.LEFT) "L1" else "R1"
        return if (side == JoyconSide.DUAL) {
            val stickLines = mapping.toStickMap<WiimoteStick>().flatMap { (_, source) -> nunchukStickLines(source) }
            buttonLines + listOf("IMUIR/Recenter = `$recenterSpec`", "Extension = Nunchuk") + stickLines
        } else {
            buttonLines + listOf("IMUIR/Recenter = `$recenterSpec`", "Extension = None") + nativeStickDPad
        }
    }

    private fun nunchukStickLines(source: StickSource): List<String> {
        val prefix = if (source == StickSource.LEFT_STICK) "Left" else "Right"
        return listOf(
            "Nunchuk/Stick/Up = `$prefix Y+`",
            "Nunchuk/Stick/Down = `$prefix Y-`",
            "Nunchuk/Stick/Left = `$prefix X-`",
            "Nunchuk/Stick/Right = `$prefix X+`",
        )
    }

    // Applies the same physical -> virtual remap SidewaysMapper uses for live HID output, so a
    // customized source resolves to the control name the DSU device would actually see for that body.
    private fun specFor(side: JoyconSide, physical: JoyconButton): String? {
        val virtualId = when (side) {
            JoyconSide.DUAL -> physical.id
            JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(physical.id)).first()
            JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(physical.id)).first()
        }
        val virtual = JoyconButton.entries.firstOrNull { it.id == virtualId } ?: return null
        return DS4_NAMES[virtual] ?: PAD_NAMES[virtual]
    }
}
