package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.DolphinControls
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Dolphin's `WiimoteNew.ini` bindings for the DSU device, one `[WiimoteN]` section per
 * assigned player, driven by the user's own Joy-Con → Wiimote/Nunchuk mapping. What it writes and
 * why, measurements included: docs/dsu-motion.md#dolphin-wii-remote-mapping.
 */
object DolphinWiimoteConfig {
    val path = DolphinPaths.config("WiimoteNew.ini")

    // Dolphin's 25 clamps the cursor after +-12.5 degrees of turn, which a hand-held aim overruns.
    private const val IMU_TOTAL_YAW_DEGREES = 60

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
        JoyconButton.Capture to "Touch Button",
    )

    // The D-Pad target reads the DS4 pad nibble instead of a face button.
    private val PAD_NAMES = mapOf(
        JoyconButton.Up to "Pad N",
        JoyconButton.Down to "Pad S",
        JoyconButton.Left to "Pad W",
        JoyconButton.Right to "Pad E",
    )

    private val ACCEL_DIRECTIONS = listOf("Up", "Down", "Left", "Right", "Forward", "Backward")

    private val GYRO_DIRECTIONS =
        listOf("Pitch Up", "Pitch Down", "Roll Left", "Roll Right", "Yaw Left", "Yaw Right")

    private val IMU_CONTROLS = ACCEL_DIRECTIONS.map { "IMUAccelerometer/$it" to "Accel $it" } +
        GYRO_DIRECTIONS.map { "IMUGyroscope/$it" to "Gyro $it" }

    // A lone Joy-Con streams in its sideways grip (SidewaysMotion); these turn it back about the
    // button face onto the body the player actually aims. Two tables because the bodies rotate into
    // their grips opposite ways: docs/dsu-motion.md#sideways-joy-cons.
    private val SIDEWAYS_REMOTE_INPUTS = mapOf(
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

    // Only a right Joy-Con gives up its own body to steer true; a left one already is a sideways
    // remote, its nose and its L/ZL edge pointing the same way, so it needs no turn either way.
    private fun bodyInputs(side: JoyconSide, sidewaysRemote: Boolean): Map<String, String> = when (side) {
        JoyconSide.DUAL -> emptyMap()
        JoyconSide.LEFT -> SIDEWAYS_REMOTE_INPUTS
        JoyconSide.RIGHT -> if (sidewaysRemote) SIDEWAYS_REMOTE_INPUTS else RIGHT_BODY_INPUTS
    }

    // The player's up is a sideways remote's right. Dolphin's own option would turn the
    // accelerometer a second time, so it stays off and these turn the bindings instead.
    private val SIDEWAYS_DPAD_KEYS = mapOf(
        WiimoteButton.DPadUp to "D-Pad/Right",
        WiimoteButton.DPadRight to "D-Pad/Down",
        WiimoteButton.DPadDown to "D-Pad/Left",
        WiimoteButton.DPadLeft to "D-Pad/Up",
    )

    private fun dolphinKey(target: WiimoteButton, sideways: Boolean): String =
        (if (sideways) SIDEWAYS_DPAD_KEYS[target] else null) ?: DOLPHIN_KEYS.getValue(target)

    // A flick is fired from the gyroscope and delivered as a jerk of the accelerometer, because a
    // Joy-Con flick carries almost no linear jerk and Mario Kart Wii reads only the accelerometer.
    // Every constant is measured: docs/dsu-motion.md#sideways-joy-cons.
    private const val FLICK_RADIANS = 9
    private const val FLICK_LOCKOUT_SECONDS = 0.4
    private const val TRICK_ACCELERATION = 50 // m/s^2, past what an emulated remote can report
    private const val TRICK_SECONDS = 0.6
    private const val TRICK_PERIOD_SECONDS = 0.15
    private const val FULL_TURN = 6.2832

    // A wheelie is a state an up-flick starts and a down-flick drops, so unlike a trick it needs the
    // direction the player flicked. Pitch carries it on both bodies; the remote is jerked the same
    // way it was flicked.
    private const val UP = "IMUAccelerometer/Up"
    private val TRICK_AXES = mapOf(UP to ("Pitch Up" to "Pitch Down"), "IMUAccelerometer/Down" to ("Pitch Down" to "Pitch Up"))

    /**
     * Each direction locks the other out: every flick rebounds the opposite way about a quarter of a
     * second later, and that rebound would otherwise answer the gesture and cancel the wheelie.
     * Gating the pulse's input rather than its output lets a jerk already running finish.
     */
    private fun trickTrigger(
        side: JoyconSide,
        control: String,
        sidewaysRemote: Boolean,
        bound: List<MappingSource>?,
    ): String? {
        val (own, opposite) = TRICK_AXES[control] ?: return null
        val flick = if (sidewaysRemote) {
            "(`Gyro $own` / $FLICK_RADIANS) & not(pulse(`Gyro $opposite` / $FLICK_RADIANS, $FLICK_LOCKOUT_SECONDS))"
        } else {
            null
        }
        val pressed = bound?.takeIf { control == UP }?.let { expressionFor(side, it) }
        return listOfNotNull(flick, pressed).takeIf { it.isNotEmpty() }?.joinToString(" | ")
    }

    // Half a wave, so the jerks all go the way the flick did — a full one would cancel the wheelie
    // it just started, four times a second.
    private fun trickShake(trigger: String?): String? = trigger?.let {
        "pulse($it, $TRICK_SECONDS) * max(sin(timer($TRICK_PERIOD_SECONDS) * $FULL_TURN), 0) * $TRICK_ACCELERATION"
    }

    private fun imuLines(
        side: JoyconSide,
        sidewaysRemote: Boolean,
        bound: List<MappingSource>?,
    ): List<String> {
        val bodyInputs = bodyInputs(side, sidewaysRemote)
        return IMU_CONTROLS.map { (control, input) ->
            val read = "`${bodyInputs[input] ?: input}`"
            val shake = trickShake(trickTrigger(side, control, sidewaysRemote, bound))
            "$control = " + (shake?.let { "$read + $it" } ?: read)
        } + listOf("IMUIR/Enabled = True", "IMUIR/Total Yaw = $IMU_TOTAL_YAW_DEGREES")
    }

    // Swing is the only way a thrust toward the sensor bar reaches a game, and it is signed and
    // high-passed because an accelerometer cannot tell one from a tilted grip:
    // docs/dsu-motion.md#dolphin-wii-remote-mapping.
    private fun swingLines(side: JoyconSide, sidewaysRemote: Boolean): List<String> {
        // A push toward the screen runs along the remote's nose, whichever input that body reads it from.
        val body = bodyInputs(side, sidewaysRemote)
        val thrust = body["Accel Forward"] ?: "Accel Forward"
        val pull = body["Accel Backward"] ?: "Accel Backward"
        val signed = "(`$thrust` - `$pull`)"
        return listOf(
            "Swing/Forward = $signed - smooth($signed, $SWING_SETTLE_SECONDS)",
            "Swing/Forward/Range = $SWING_RANGE_PERCENT",
            "Swing/Dead Zone = $SWING_DEAD_ZONE_PERCENT",
        )
    }

    // Dolphin splits a control on its last colon, so `<device>:<input>` reaches the second hand's
    // slot. A real Nunchuk has no gyroscope, only this accel.
    private fun nunchukImuLines(slot: Int): List<String> =
        ACCEL_DIRECTIONS.map { "Nunchuk/IMUAccelerometer/$it = `DSUClient/$slot/Joycon2:Accel $it`" }

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        sidewaysRemoteFor: (PlayerBody) -> Boolean,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String = IniEditor.mergeSections(existing, sections(players, sidewaysRemoteFor, mappingFor))

    private fun sections(
        players: List<PlayerState>,
        sidewaysRemoteFor: (PlayerBody) -> Boolean,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): Map<String, String> {
        val secondHands = DsuSlots.secondHands(players).associate { it.state.player to it.slot }
        return players.mapNotNull { player ->
            val slot = player.player.index - 1
            if (slot !in 0..3) return@mapNotNull null
            bodyFor(player, slot, secondHands[player.player], sidewaysRemoteFor, mappingFor)
                ?.let { "[Wiimote${player.player.index}]" to it }
        }.toMap()
    }

    private fun bodyFor(
        player: PlayerState,
        slot: Int,
        secondHandSlot: Int?,
        sidewaysRemoteFor: (PlayerBody) -> Boolean,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String? {
        val side = when {
            player.hasPro -> return null
            player.hasFullController -> JoyconSide.DUAL
            player.right != null -> JoyconSide.RIGHT
            player.left != null -> JoyconSide.LEFT
            else -> return null
        }
        val body = PlayerBody(player.player, side)
        val sidewaysRemote = sidewaysRemoteFor(body)
        // Source = 1 forces this Wii Remote slot to Emulated, so the mappings actually apply
        val header = listOf("Source = 1", "Device = DSUClient/$slot/Joycon2")
        val nunchukImu = if (side == JoyconSide.DUAL && secondHandSlot != null) {
            nunchukImuLines(secondHandSlot)
        } else {
            emptyList()
        }
        val sideways = sidewaysRemote && side != JoyconSide.DUAL
        val mapping = mappingFor(body)
        val shake = mapping.toSourceMap<WiimoteButton>()[WiimoteButton.Shake]
        return (header + lines(side, sideways, mapping) + imuLines(side, sidewaysRemote, shake) +
            swingLines(side, sidewaysRemote) + nunchukImu)
            .joinToString("\n", postfix = "\n")
    }

    private fun lines(side: JoyconSide, sideways: Boolean, mapping: Map<String, String>): List<String> {
        val buttonLines = (mapping.toSourceMap<WiimoteButton>() - WiimoteButton.Shake)
            .mapNotNull { (target, sources) ->
                expressionFor(side, sources)?.let { expression -> "${dolphinKey(target, sideways)} = $expression" }
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
            directions.mapNotNull { (direction, sources) ->
                expressionFor(side, sources)?.let { expression -> "Nunchuk/Stick/${DolphinControls.DIRECTIONS.getValue(direction)} = $expression" }
            }
        }

    // Dolphin's expression language ORs its inputs, so every source bound to a target can fire it.
    private fun expressionFor(side: JoyconSide, sources: List<MappingSource>): String? =
        sources.mapNotNull { specFor(side, it) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" | ") { "`$it`" }

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
