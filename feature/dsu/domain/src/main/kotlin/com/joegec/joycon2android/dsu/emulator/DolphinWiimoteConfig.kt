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

    // Dolphin clamps the pointer's accumulated yaw to half of this, and its 25 degrees is a living
    // room's worth: a captured aiming session (2026-09) swung +-20 to 25, so the cursor spent its
    // time pinned against the clamp. Recenter (below) is what pulls it back when it drifts.
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

    // A lone Joy-Con streams in its sideways grip (SidewaysMotion); turning that grip back about the
    // button face restores the Joy-Con's own body, which is the remote the player aims down its
    // shoulder edge. The bodies rotate into their grips opposite ways, so their tables are each
    // other half a turn — and a LEFT Joy-Con's own body already *is* a Wii Remote held sideways,
    // since a sideways remote's nose points left just as its L/ZL edge does.
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

    // So the sideways-remote layouts change one body's motion: a right Joy-Con gives up its own
    // body — and with it the R/ZR edge as the nose, aiming moving to the tail — to read gravity the
    // way a wheel game expects. A left Joy-Con needs no turn either way.
    private fun bodyInputs(side: JoyconSide, sidewaysRemote: Boolean): Map<String, String> = when (side) {
        JoyconSide.DUAL -> emptyMap()
        JoyconSide.LEFT -> SIDEWAYS_REMOTE_INPUTS
        JoyconSide.RIGHT -> if (sidewaysRemote) SIDEWAYS_REMOTE_INPUTS else RIGHT_BODY_INPUTS
    }

    // A sideways remote's d-pad turns with it: the player's up is the remote's right. Dolphin does
    // this itself (dpad_sideways_bitmasks) when its Sideways Wii Remote option is on, but that
    // option also turns the accelerometer a quarter, which our own table has already done — so the
    // option stays off and the four bindings are turned here instead.
    private val SIDEWAYS_DPAD_KEYS = mapOf(
        WiimoteButton.DPadUp to "D-Pad/Right",
        WiimoteButton.DPadRight to "D-Pad/Down",
        WiimoteButton.DPadDown to "D-Pad/Left",
        WiimoteButton.DPadLeft to "D-Pad/Up",
    )

    private fun dolphinKey(target: WiimoteButton, sideways: Boolean): String =
        (if (sideways) SIDEWAYS_DPAD_KEYS[target] else null) ?: DOLPHIN_KEYS.getValue(target)

    // A trick is a flick, and a flick of something Joy-Con sized is mostly rotation: captured ones
    // peak past 1200 deg/s summed while carrying barely a g of linear jerk, where jerking a real Wii
    // Wheel throws the whole thing. Mario Kart Wii has no MotionPlus and reads only the
    // accelerometer, so the flick never reaches it — on hardware it wouldn't either. The gyroscope
    // therefore fires the trick, which hardware could not do. Summing each axis with its opposite
    // input gives |rate|, since Dolphin clamps one of any pair at zero.
    //
    // Dolphin's own Shake group is not the way to deliver it: bound straight to a key in Dolphin's
    // config, a full 7 g oscillation of it never landed a trick (tested 2026-09). The accelerometer
    // is the path that demonstrably reaches the game, since steering is read from it, so the jerk
    // goes there — onto one input of each pair, a diagonal no axis can miss, with the opposites left
    // alone so the pair cannot cancel it.
    //
    // It is a shake, not a push. What actually landed one (2026-09) was shaking a Joy-Con hard for
    // about a second, so the synthetic trick copies that shape: an oscillation held for
    // TRICK_SECONDS, with each input of a pair swung in antiphase so the remote is thrown back and
    // forth rather than leaned on. Amplitude is not the lever — an emulated Wii Remote saturates
    // around +3.9/-4.9 g (ACCEL_ZERO_G 0x80, ACCEL_ONE_G 0x9A over 8 bits), which
    // TRICK_ACCELERATION already passes — so a bigger number only clips sooner. Duration and
    // swinging are what a held push was missing, and pulse() gives a flick and a held button the
    // same one however long either lasted.
    //
    // A rate alone cannot tell a flick from a turn, because steering a lone Joy-Con held as a wheel
    // *is* rotation — which is why only single Joy-Cons suffered for it: a pair steers from the
    // Nunchuk's stick with its remote hand still. Subtracting a slew limiter leaves only what climbs
    // faster than the limiter can follow.
    //
    // Both numbers are measured, over a capture of flicks and a capture of hard steering read back
    // by tools/flick_stats.py (2026-09-22, right Joy-Con, 15 ms stream). Flicks peaked at 11 to 16
    // rad/s and left 5.6 to 9.1 behind the limiter; 25 s of the sharpest steering peaked at 3.6 and
    // left at most 1.2. A slower limiter is worse, not better: it lifts a flick's residual but lifts
    // steering's faster, and the ratio between them — all that matters — falls from 4.7 at 0.01 to
    // 3.8 at 0.02 and 2.0 at 0.04.
    //
    // pulse() fires as its input crosses a half, so the threshold is 2.5 rad/s of residual: 2.1x
    // above the worst steering and 2.2x below the weakest flick, which is as evenly as two sparsely
    // sampled distributions can be split. Erring low is right anyway — a trick fired by accident
    // costs nothing, since the game only tricks a kart already airborne, while one fired *while
    // steering* costs plenty, the shake landing on the accelerometer the wheel is read from.
    private const val FLICK_RADIANS = 5
    private const val FLICK_SETTLE_SECONDS = 0.01
    private const val TRICK_ACCELERATION = 50 // m/s^2, past what an emulated remote can report
    private const val TRICK_SECONDS = 0.6
    private const val TRICK_PERIOD_SECONDS = 0.15
    private const val FULL_TURN = 6.2832
    private const val HALF_TURN = 3.1416

    // The three that lead; their opposites follow half a cycle later, which is the swing.
    private val TRICK_LEADING =
        setOf("IMUAccelerometer/Up", "IMUAccelerometer/Left", "IMUAccelerometer/Forward")

    /**
     * What fires a trick: a flick, and whatever is bound to Shake. Every body flicks, a pair
     * included — its remote hand is still while the Nunchuk's stick does the steering — but only
     * while the layout plays as a sideways remote, so no other game is handed a shake it never asked
     * for when its remote is swung.
     */
    private fun shakeTrigger(side: JoyconSide, sidewaysRemote: Boolean, bound: List<MappingSource>?): String? {
        val rate = "(${GYRO_DIRECTIONS.joinToString(" + ") { "`Gyro $it`" }})"
        val flick = if (sidewaysRemote) "($rate - smooth($rate, $FLICK_SETTLE_SECONDS)) / $FLICK_RADIANS" else null
        return listOfNotNull(flick, bound?.let { expressionFor(side, it) })
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" | ")
    }

    private fun trickShake(trigger: String?, control: String): String? {
        if (trigger == null || !control.startsWith("IMUAccelerometer/")) return null
        val phase = if (control in TRICK_LEADING) "" else " + $HALF_TURN"
        return "pulse($trigger, $TRICK_SECONDS) * " +
            "sin(timer($TRICK_PERIOD_SECONDS) * $FULL_TURN$phase) * $TRICK_ACCELERATION"
    }

    private fun imuLines(side: JoyconSide, sidewaysRemote: Boolean, trigger: String?): List<String> {
        val bodyInputs = bodyInputs(side, sidewaysRemote)
        return IMU_CONTROLS.map { (control, input) ->
            val read = "`${bodyInputs[input] ?: input}`"
            "$control = " + (trickShake(trigger, control)?.let { "$read + $it" } ?: read)
        } + listOf("IMUIR/Enabled = True", "IMUIR/Total Yaw = $IMU_TOTAL_YAW_DEGREES")
    }

    // Dolphin's emulated remote only ever translates through the Swing group — the IMU path feeds
    // rotation alone — so the virtual remote stays pinned in space and the IR dots never change
    // separation. Games that read a thrust as distance to the sensor bar (Wii Play Billiards charges
    // cue strength that way) see nothing from accel and gyro alone. A push toward the screen lands
    // on whichever input the remote's nose reads; pairing it with its opposite makes the value
    // signed, since Dolphin clamps a single input at zero.
    //
    // An accelerometer cannot tell gravity from sustained acceleration, so a tilted grip parks up to
    // 1 g on that axis and Swing reads it as a thrust held forever. smooth() is a slew limiter, so
    // subtracting it high-passes the axis: the tracker catches a static tilt within a third of a
    // second and cancels it, while a thrust's ~80 ms transient outruns it. Range then trims the
    // inputs, which arrive at 9.8 per g, to a full-distance lunge at roughly a 1.5 g thrust.
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

    // A pad packet carries one IMU, so the Nunchuk hand streams on a slot of its own and the
    // extension reads it across devices: Dolphin splits a control on its last colon, so
    // `<device>:<input>` reaches another pad. A real Nunchuk has no gyroscope, only this accel.
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
        val trigger = shakeTrigger(side, sidewaysRemote, mapping.toSourceMap<WiimoteButton>()[WiimoteButton.Shake])
        return (header + lines(side, sideways, mapping) + imuLines(side, sidewaysRemote, trigger) +
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
                expressionFor(side, sources)?.let { expression -> "Nunchuk/Stick/${direction.displayName} = $expression" }
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
