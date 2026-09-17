package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Dolphin's WiimoteNew.ini button mappings for the DSU device, one `[WiimoteN]`
 * section per assigned player (player N streams on DSU slot N-1 → `DSUClient/<slot>/Joycon2`,
 * the name matching our [DolphinDsuConfig] entry), driven by the user's customizable Joy-Con ->
 * Wiimote/Nunchuk mapping. By default a single sideways Joy-Con drives the D-pad target from its
 * own analog stick with no extension, and a pair drives it from the physical D-pad and exposes its
 * left stick as the Nunchuk; a single Joy-Con gains a Nunchuk once the user binds one of its controls. [DS4_NAMES]/[PAD_NAMES] are the fixed, body-independent DS4-convention names the DSU
 * device exposes for each Android input (see the in-app mapping table); [specFor] resolves a
 * customized source to the one its body actually emits.
 */
object DolphinWiimoteConfig {
    val path = DolphinPaths.config("WiimoteNew.ini")

    // Keeps a tilted grip's gravity leak from nudging the virtual remote off its neutral position.
    private const val SWING_DEAD_ZONE_PERCENT = 20
    private const val SWING_RANGE_PERCENT = 7
    private const val SWING_SETTLE_SECONDS = 0.03

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
        JoyconButton.Capture to "Touch",
    )

    // The D-Pad target reads the DS4 pad nibble instead of a face button.
    private val PAD_NAMES = mapOf(
        JoyconButton.Up to "Pad N",
        JoyconButton.Down to "Pad S",
        JoyconButton.Left to "Pad W",
        JoyconButton.Right to "Pad E",
    )

    private val ACCEL_DIRECTIONS = listOf("Up", "Down", "Left", "Right", "Forward", "Backward")

    private val IMU_CONTROLS = ACCEL_DIRECTIONS.map { "IMUAccelerometer/$it" to "Accel $it" } +
        listOf("Pitch Up", "Pitch Down", "Roll Left", "Roll Right", "Yaw Left", "Yaw Right")
            .map { "IMUGyroscope/$it" to "Gyro $it" }

    // A lone Joy-Con streams in its sideways grip (SidewaysMotion), but the emulated remote is the
    // Joy-Con's own body, so its inputs turn back about the button face. Up/Down and yaw lie on
    // that axis and pass through name-to-name.
    private val LEFT_BODY_INPUTS = mapOf(
        "Accel Left" to "Accel Backward", "Accel Right" to "Accel Forward",
        "Accel Forward" to "Accel Left", "Accel Backward" to "Accel Right",
        "Gyro Pitch Up" to "Gyro Roll Right", "Gyro Pitch Down" to "Gyro Roll Left",
        "Gyro Roll Left" to "Gyro Pitch Up", "Gyro Roll Right" to "Gyro Pitch Down",
    )
    private val RIGHT_BODY_INPUTS = mapOf(
        "Accel Left" to "Accel Forward", "Accel Right" to "Accel Backward",
        "Accel Forward" to "Accel Right", "Accel Backward" to "Accel Left",
        "Gyro Pitch Up" to "Gyro Roll Left", "Gyro Pitch Down" to "Gyro Roll Right",
        "Gyro Roll Left" to "Gyro Pitch Down", "Gyro Roll Right" to "Gyro Pitch Up",
    )

    private fun imuLines(side: JoyconSide): List<String> {
        val bodyInputs = when (side) {
            JoyconSide.LEFT -> LEFT_BODY_INPUTS
            JoyconSide.RIGHT -> RIGHT_BODY_INPUTS
            JoyconSide.DUAL -> emptyMap()
        }
        return IMU_CONTROLS.map { (control, input) -> "$control = `${bodyInputs[input] ?: input}`" } +
            "IMUIR/Enabled = True"
    }

    // Dolphin's emulated remote only ever translates through the Swing group — the IMU path feeds
    // rotation alone — so the virtual remote stays pinned in space and the IR dots never change
    // separation. Games that read a thrust as distance to the sensor bar (Wii Play Billiards charges
    // cue strength that way) see nothing from accel and gyro alone. A push toward the screen lands
    // on `Accel Forward` for a pair held like a Wii Remote, and on `Accel Up` for a solo sideways
    // Joy-Con (out through the button face); pairing each with its opposite input makes the value
    // signed, since Dolphin clamps a single input at zero.
    //
    // An accelerometer cannot tell gravity from sustained acceleration, so a tilted grip parks up to
    // 1 g on that axis and Swing reads it as a thrust held forever. smooth() is a slew limiter, so
    // subtracting it high-passes the axis: the tracker catches a static tilt within a third of a
    // second and cancels it, while a thrust's ~80 ms transient outruns it. Range then trims the
    // inputs, which arrive at 9.8 per g, to a full-distance lunge at roughly a 1.5 g thrust.
    private fun swingLines(side: JoyconSide): List<String> {
        val thrust = if (side == JoyconSide.DUAL) "Accel Forward" else "Accel Up"
        val pull = if (side == JoyconSide.DUAL) "Accel Backward" else "Accel Down"
        val signed = "(`$thrust` - `$pull`)"
        return listOf(
            "Swing/Forward = $signed - smooth($signed, $SWING_SETTLE_SECONDS)",
            "Swing/Forward/Range = $SWING_RANGE_PERCENT",
            "Swing/Dead Zone = $SWING_DEAD_ZONE_PERCENT",
        )
    }

    // A pad packet carries one IMU, so the Nunchuk hand streams on a slot of its own and the
    // extension reads it across devices: Dolphin splits a control on its last colon, so
    // `<device>:<input>` reaches another pad. A real Nunchuk has no gyroscope, only this accel.
    private fun nunchukImuLines(slot: Int): List<String> =
        ACCEL_DIRECTIONS.map { "Nunchuk/IMUAccelerometer/$it = `DSUClient/$slot/Joycon2:Accel $it`" }

    fun merge(existing: String?, players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): String =
        IniEditor.mergeSections(existing, sections(players, mappingFor))

    private fun sections(players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): Map<String, String> {
        val secondHands = DsuSlots.secondHands(players).associate { it.state.player to it.slot }
        return players.mapNotNull { player ->
            val slot = player.player.index - 1
            if (slot !in 0..3) return@mapNotNull null
            bodyFor(player, slot, secondHands[player.player], mappingFor)
                ?.let { "[Wiimote${player.player.index}]" to it }
        }.toMap()
    }

    private fun bodyFor(
        player: PlayerState,
        slot: Int,
        secondHandSlot: Int?,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): String? {
        val side = when {
            player.hasPro -> return null
            player.hasFullController -> JoyconSide.DUAL
            player.right != null -> JoyconSide.RIGHT
            player.left != null -> JoyconSide.LEFT
            else -> return null
        }
        // Source = 1 forces this Wii Remote slot to Emulated, so the mappings actually apply
        val header = listOf("Source = 1", "Device = DSUClient/$slot/Joycon2")
        val nunchukImu = if (side == JoyconSide.DUAL && secondHandSlot != null) {
            nunchukImuLines(secondHandSlot)
        } else {
            emptyList()
        }
        return (header + lines(side, mappingFor(side)) + imuLines(side) + swingLines(side) + nunchukImu)
            .joinToString("\n", postfix = "\n")
    }

    private fun lines(side: JoyconSide, mapping: Map<String, String>): List<String> {
        val buttonLines = mapping.toSourceMap<WiimoteButton>().mapNotNull { (target, source) ->
            specFor(side, source)?.let { spec -> "${DOLPHIN_KEYS.getValue(target)} = `$spec`" }
        }
        val stickLines = nunchukStickLines(side, mapping)
        val recenterSpec = if (side == JoyconSide.LEFT) "L1" else "R1"
        val extension = if (usesNunchuk(side, buttonLines + stickLines)) "Nunchuk" else "None"
        return buttonLines + listOf("IMUIR/Recenter = `$recenterSpec`", "Extension = $extension") + stickLines
    }

    // A pair always plugs one in for its second hand; a lone Joy-Con only once a Nunchuk control is bound.
    private fun usesNunchuk(side: JoyconSide, mappedLines: List<String>) =
        side == JoyconSide.DUAL || mappedLines.any { it.startsWith("Nunchuk/") }

    private fun nunchukStickLines(side: JoyconSide, mapping: Map<String, String>): List<String> =
        mapping.toStickDirectionMap<WiimoteStick>().values.flatMap { directions ->
            directions.mapNotNull { (direction, source) ->
                specFor(side, source)?.let { spec -> "Nunchuk/Stick/${direction.displayName} = `$spec`" }
            }
        }

    private fun specFor(side: JoyconSide, source: MappingSource): String? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side)?.let { DS4_NAMES[it] ?: PAD_NAMES[it] }
        is MappingSource.Stick -> tiltSpec(source.emittedStick(side), source.direction)
    }

    // DSU sticks report up as a positive Y, unlike Android's axes.
    private fun tiltSpec(stick: StickSource, direction: StickDirection): String {
        val prefix = if (stick == StickSource.LEFT_STICK) "Left" else "Right"
        return when (direction) {
            StickDirection.UP -> "$prefix Y+"
            StickDirection.DOWN -> "$prefix Y-"
            StickDirection.LEFT -> "$prefix X-"
            StickDirection.RIGHT -> "$prefix X+"
        }
    }
}
