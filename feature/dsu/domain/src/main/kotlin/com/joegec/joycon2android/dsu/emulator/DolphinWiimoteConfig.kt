package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.DolphinControls
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.PlayerState
import java.math.BigDecimal
import kotlin.math.abs

/** What it writes and why: docs/dsu-motion.md#dolphin-wii-remote-mapping */
object DolphinWiimoteConfig {
    val path = DolphinPaths.config("WiimoteNew.ini")

    // Dolphin's 25 clamps the cursor after +-12.5 degrees of turn, which a hand-held aim overruns.
    private const val IMU_TOTAL_YAW_DEGREES = 60

    // Keeps a tilted grip's gravity leak from nudging the virtual remote off its neutral position.
    private const val SWING_DEAD_ZONE_PERCENT = 20
    private const val SWING_RANGE_PERCENT = 7

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
        WiimoteButton.Recenter to "IMUIR/Recenter",
    )

    private val ACCEL_DIRECTIONS = listOf("Up", "Down", "Left", "Right", "Forward", "Backward")

    private val GYRO_DIRECTIONS =
        listOf("Pitch Up", "Pitch Down", "Roll Left", "Roll Right", "Yaw Left", "Yaw Right")

    private val IMU_CONTROLS = ACCEL_DIRECTIONS.map { "IMUAccelerometer/$it" to "Accel $it" } +
        GYRO_DIRECTIONS.map { "IMUGyroscope/$it" to "Gyro $it" }

    // Turn a lone Joy-Con's sideways stream back onto the body it aims: docs/dsu-motion.md#sideways-joy-cons
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

    // A left Joy-Con's L/ZL edge is already a sideways remote's nose, so only a right one differs.
    private fun bodyInputs(side: JoyconSide, sidewaysRemote: Boolean): Map<String, String> = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> emptyMap()
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

    // Measured: docs/dsu-motion.md#tricks-and-wheelies
    private const val FLICK_RADIANS = 9
    private const val FLICK_LOCKOUT_SECONDS = 0.4
    private const val TRICK_ACCELERATION = 50 // m/s^2, past what an emulated remote can report
    private const val TRICK_SECONDS = 0.6
    private const val TRICK_PERIOD_SECONDS = 0.15
    private const val FULL_TURN = 6.2832

    private const val UP = "IMUAccelerometer/Up"
    private val TRICK_AXES = mapOf(UP to ("Pitch Up" to "Pitch Down"), "IMUAccelerometer/Down" to ("Pitch Down" to "Pitch Up"))

    // Each direction locks out the other, so a flick's rebound can't cancel the wheelie.
    private fun trickTrigger(
        side: JoyconSide,
        control: String,
        flicks: Boolean,
        bound: List<MappingSource>?,
    ): String? {
        val (own, opposite) = TRICK_AXES[control] ?: return null
        val flick = if (flicks) {
            "(`Gyro $own` / $FLICK_RADIANS) & not(pulse(`Gyro $opposite` / $FLICK_RADIANS, $FLICK_LOCKOUT_SECONDS))"
        } else {
            null
        }
        val pressed = bound?.takeIf { control == UP }?.let { DolphinDsuInputs.expressionFor(side, flicks, it) }
        return listOfNotNull(flick, pressed).takeIf { it.isNotEmpty() }?.joinToString(" | ")
    }

    // Half-rectified, so every jerk goes the way the flick did.
    private fun trickShake(trigger: String?): String? = trigger?.let {
        "pulse($it, $TRICK_SECONDS) * max(sin(timer($TRICK_PERIOD_SECONDS) * $FULL_TURN), 0) * $TRICK_ACCELERATION"
    }

    private fun imuLines(
        side: JoyconSide,
        motion: RemoteMotion,
        bound: List<MappingSource>?,
    ): List<String> {
        val bodyInputs = bodyInputs(side, motion.heldSideways)
        return IMU_CONTROLS.map { (control, input) ->
            val read = "`${bodyInputs[input] ?: input}`"
            val boost = sensitivityBoost(control, motion.sensitivityPercent) { "`${bodyInputs["Accel $it"] ?: "Accel $it"}`" }
            val flicks = motion.controller == WiiController.SIDEWAYS_REMOTE
            val shake = trickShake(trickTrigger(side, control, flicks, bound))?.let { "+ $it" }
            "$control = " + listOfNotNull(read, boost, shake).joinToString(" ")
        } + listOf("IMUIR/Enabled = True", "IMUIR/Total Yaw = $IMU_TOTAL_YAW_DEGREES")
    }

    // Dolphin reads each axis as one control minus its opposite, so boosting one control scales the axis.
    private val BOOSTED_ACCEL_AXES = mapOf("Up" to "Down", "Left" to "Right", "Forward" to "Backward")

    /** Scales motion and leaves gravity alone: docs/dsu-motion.md#motion-sensitivity */
    private fun sensitivityBoost(control: String, percent: Int, read: (direction: String) -> String): String? {
        val direction = control.substringAfterLast("IMUAccelerometer/")
        val opposite = BOOSTED_ACCEL_AXES[direction] ?: return null
        val gain = percent - RemoteMotion.UNSCALED_SENSITIVITY_PERCENT
        if (gain == 0) return null
        val signed = "(${read(direction)} - ${read(opposite)})"
        val sign = if (gain > 0) "+" else "-"
        val fraction = BigDecimal(abs(gain)).movePointLeft(2).stripTrailingZeros().toPlainString()
        return "$sign $fraction * ${DolphinHighPass.of(signed, control)}"
    }

    private const val SWING_FORWARD = "Swing/Forward"

    // Dolphin adds the swing it emulates to the accelerometer, so only a game that wants thrusts gets one.
    private fun swingLines(side: JoyconSide, motion: RemoteMotion): List<String> {
        if (!motion.thrust) return emptyList()
        val body = bodyInputs(side, motion.heldSideways)
        val thrust = body["Accel Forward"] ?: "Accel Forward"
        val pull = body["Accel Backward"] ?: "Accel Backward"
        val signed = "(`$thrust` - `$pull`)"
        return listOf(
            "$SWING_FORWARD = ${DolphinHighPass.of(signed, SWING_FORWARD)}",
            "Swing/Forward/Range = $SWING_RANGE_PERCENT",
            "Swing/Dead Zone = $SWING_DEAD_ZONE_PERCENT",
        )
    }

    // Dolphin splits on the last colon, so `<device>:<input>` reads the second hand's slot.
    private fun nunchukImuLines(slot: Int, sensitivityPercent: Int): List<String> {
        val read = { direction: String -> "`DSUClient/$slot/Joycon2:Accel $direction`" }
        return ACCEL_DIRECTIONS.map { direction ->
            val control = "Nunchuk/IMUAccelerometer/$direction"
            val boost = sensitivityBoost(control, sensitivityPercent, read)
            "$control = " + listOfNotNull(read(direction), boost).joinToString(" ")
        }
    }

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        motionFor: (PlayerBody) -> RemoteMotion,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String {
        val sections = sections(players, motionFor, mappingFor)
        return IniEditor.mergeSections(existing, StaleDolphinWiimotes.disconnected(existing, sections.keys) + sections)
    }

    /** Pro and GameCube controllers are left to the player's own Dolphin mapping, unless they play a Classic Controller. */
    fun writesFor(player: PlayerState, motionFor: (PlayerBody) -> RemoteMotion): Boolean {
        val body = player.body() ?: return false
        return player.player.index - 1 in 0 until DsuSlots.COUNT &&
            (!player.hasPro || motionFor(body).controller == WiiController.CLASSIC)
    }

    private fun sections(
        players: List<PlayerState>,
        motionFor: (PlayerBody) -> RemoteMotion,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): Map<String, String> {
        val secondHands = DsuSlots.secondHands(players).associate { it.state.player to it.slot }
        return players.filter { writesFor(it, motionFor) }.mapNotNull { it.body() }.associate { body ->
            "[Wiimote${body.player.index}]" to bodyFor(body, secondHands[body.player], motionFor, mappingFor)
        }
    }

    private fun bodyFor(
        body: PlayerBody,
        secondHandSlot: Int?,
        motionFor: (PlayerBody) -> RemoteMotion,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String {
        val side = body.side
        val mapping = mappingFor(body)
        // Source = 1 forces this Wii Remote slot to Emulated, so the mappings actually apply
        val header = listOf("Source = 1", "Device = DSUClient/${body.player.index - 1}/Joycon2")
        val motion = motionFor(body)
        if (motion.controller == WiiController.CLASSIC) {
            return (header + DolphinClassicController.lines(side, mapping) + classicPointerLines(side, motion))
                .joinToString("\n", postfix = "\n")
        }
        val sidewaysRemote = motion.heldSideways
        val nunchukImu = if (side == JoyconSide.DUAL && secondHandSlot != null) {
            nunchukImuLines(secondHandSlot, motion.sensitivityPercent)
        } else {
            emptyList()
        }
        val shake = mapping.toSourceMap<WiimoteButton>()[WiimoteButton.Shake]
        return (header + lines(side, sidewaysRemote, mapping) + imuLines(side, motion, shake) +
            swingLines(side, motion) + nunchukImu)
            .joinToString("\n", postfix = "\n")
    }

    // The remote still points for menus; a GameCube controller has no motion to point with.
    private fun classicPointerLines(side: JoyconSide, motion: RemoteMotion): List<String> =
        if (side == JoyconSide.GAMECUBE) emptyList() else imuLines(side, motion, bound = null)

    private fun lines(side: JoyconSide, sidewaysRemote: Boolean, mapping: Map<String, String>): List<String> {
        val sideways = sidewaysRemote && side.isLone
        val buttonLines = (mapping.toSourceMap<WiimoteButton>() - WiimoteButton.Shake)
            .mapNotNull { (target, sources) ->
                DolphinDsuInputs.expressionFor(side, sidewaysRemote, sources)
                    ?.let { expression -> "${dolphinKey(target, sideways)} = $expression" }
            }
        val stickLines = nunchukStickLines(side, sidewaysRemote, mapping)
        val extension = if (usesNunchuk(side, buttonLines + stickLines)) "Nunchuk" else "None"
        return buttonLines + listOf("Extension = $extension") + stickLines
    }

    // A pair always plugs one in for its second hand; a lone Joy-Con only once a Nunchuk control is bound.
    private fun usesNunchuk(side: JoyconSide, mappedLines: List<String>) =
        side == JoyconSide.DUAL || mappedLines.any { it.startsWith("Nunchuk/") }

    private fun nunchukStickLines(side: JoyconSide, sidewaysRemote: Boolean, mapping: Map<String, String>): List<String> =
        mapping.toStickDirectionMap<WiimoteStick>().values.flatMap { directions ->
            directions.mapNotNull { (direction, sources) ->
                DolphinDsuInputs.expressionFor(side, sidewaysRemote, sources)?.let { expression -> "Nunchuk/Stick/${DolphinControls.DIRECTIONS.getValue(direction)} = $expression" }
            }
        }
}
