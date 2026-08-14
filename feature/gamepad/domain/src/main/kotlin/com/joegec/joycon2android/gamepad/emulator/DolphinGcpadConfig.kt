package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.toButtonMap
import com.joegec.joycon2android.buttonmapping.toStickMap
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.SidewaysMapper

/**
 * Generates Dolphin's GCPadNew.ini mappings for the Virtual Gamepad, one `[GCPadN]` section per
 * assigned player, driven by the user's customizable Joy-Con -> GameCube mapping. Each player's
 * UHID pad shows up to Dolphin as a distinct Android input device
 * (`Android/<n>/Joy-Con Virtual Gamepad <n>`); the relay remaps buttons/sticks by orientation
 * (see [SidewaysMapper]), so which physical button reaches a given Android control differs
 * between a sideways single Joy-Con and a pair. [ANDROID_NAMES]/[HAT_NAMES] are the fixed,
 * body-independent Dolphin names for each Android keycode/hat direction our virtual pad emits
 * (captured from a real mapping); [specFor] applies the same physical -> virtual remap
 * [SidewaysMapper] uses for live input to find the right one for a customized source.
 */
object DolphinGcpadConfig {
    val path = DolphinPaths.config("GCPadNew.ini")
    val corePath = DolphinPaths.config("Dolphin.ini")

    private const val STANDARD_CONTROLLER = "6" // Dolphin SIDevice: Standard Controller

    private val DOLPHIN_KEYS = mapOf(
        GameCubeButton.A to "Buttons/A",
        GameCubeButton.B to "Buttons/B",
        GameCubeButton.X to "Buttons/X",
        GameCubeButton.Y to "Buttons/Y",
        GameCubeButton.Z to "Buttons/Z",
        GameCubeButton.Start to "Buttons/Start",
        GameCubeButton.TriggerL to "Triggers/L",
        GameCubeButton.TriggerR to "Triggers/R",
        GameCubeButton.DPadUp to "D-Pad/Up",
        GameCubeButton.DPadDown to "D-Pad/Down",
        GameCubeButton.DPadLeft to "D-Pad/Left",
        GameCubeButton.DPadRight to "D-Pad/Right",
    )

    // Dolphin's name for each Android keycode our virtual pad emits, fixed regardless of body.
    private val ANDROID_NAMES = mapOf(
        JoyconButton.A to "Button A",
        JoyconButton.B to "Button B",
        JoyconButton.X to "Button C",
        JoyconButton.Y to "Button X",
        JoyconButton.L to "Button Y",
        JoyconButton.R to "Button Z",
        JoyconButton.ZL to "Button L1",
        JoyconButton.ZR to "Button R1",
        JoyconButton.Minus to "Button L2",
        JoyconButton.Plus to "Button R2",
        JoyconButton.Camera to "Button L3",
        JoyconButton.RS to "Start",
        JoyconButton.LS to "Select",
        JoyconButton.Home to "Mode",
    )

    // The D-Pad target reads the hat switch instead: Axis 15 = hat X, Axis 16 = hat Y.
    private val HAT_NAMES = mapOf(
        JoyconButton.Up to "Axis 16-",
        JoyconButton.Down to "Axis 16+",
        JoyconButton.Left to "Axis 15-",
        JoyconButton.Right to "Axis 15+",
    )

    // A sideways single Joy-Con's own stick isn't user-routable — there's only one.
    private val NATIVE_MAIN_STICK_LINES = listOf(
        "Main Stick/Up = `Axis 1-`",
        "Main Stick/Down = `Axis 1+`",
        "Main Stick/Left = `Axis 0-`",
        "Main Stick/Right = `Axis 0+`",
    )

    fun merge(existing: String?, players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): String =
        IniEditor.mergeSections(existing, sections(players, mappingFor))

    /** Sets each configured player's GameCube port to a Standard Controller in Dolphin.ini. */
    fun mergeCore(existing: String?, players: List<PlayerState>): String {
        val siDevices = players
            .filter { it.hasController && !it.hasPro && it.player.index in 1..4 }
            .associate { "SIDevice${it.player.index - 1}" to STANDARD_CONTROLLER }
        return IniEditor.setKeys(existing, "[Core]", siDevices)
    }

    // Dolphin's device id is the pad's enumeration rank among active virtual gamepads (1-based),
    // which is NOT the player number when a lower slot is empty (e.g. P4 with no P3 → Android/3/…).
    // The device *name* still carries the player number. Sections stay on the player's own GC port.
    private fun sections(players: List<PlayerState>, mappingFor: (JoyconSide) -> Map<String, String>): Map<String, String> =
        players.filter { it.hasController }
            .sortedBy { it.player.index }
            .mapIndexedNotNull { rank, player ->
                val index = player.player.index
                if (index !in 1..4) return@mapIndexedNotNull null
                bodyFor(player, index, deviceId = rank + 1, mappingFor)?.let { "[GCPad$index]" to it }
            }.toMap()

    private fun bodyFor(player: PlayerState, index: Int, deviceId: Int, mappingFor: (JoyconSide) -> Map<String, String>): String? {
        val side = when {
            player.hasPro -> return null
            player.hasFullController -> JoyconSide.DUAL
            player.right != null -> JoyconSide.RIGHT
            player.left != null -> JoyconSide.LEFT
            else -> return null
        }
        val device = "Device = Android/$deviceId/Joy-Con Virtual Gamepad $index"
        return (listOf(device) + lines(side, mappingFor(side))).joinToString("\n", postfix = "\n")
    }

    private fun lines(side: JoyconSide, mapping: Map<String, String>): List<String> {
        val buttonLines = mapping.toButtonMap<GameCubeButton>().mapNotNull { (target, source) ->
            specFor(side, source)?.let { spec -> "${DOLPHIN_KEYS.getValue(target)} = `$spec`" }
        }
        val stickLines = if (side == JoyconSide.DUAL) {
            mapping.toStickMap<GameCubeStick>().flatMap { (target, source) -> stickLines(target, source) }
        } else {
            NATIVE_MAIN_STICK_LINES
        }
        return buttonLines + stickLines
    }

    // Physical left stick lands on Android axes 0/1, physical right stick on axes 11/14 (see
    // ReportMapper) — fixed regardless of which target stick a source is routed to.
    private fun stickLines(target: GameCubeStick, source: StickSource): List<String> {
        val prefix = if (target == GameCubeStick.MainStick) "Main Stick" else "C-Stick"
        val (upDown, leftRight) = if (source == StickSource.LEFT_STICK) "1" to "0" else "14" to "11"
        return listOf(
            "$prefix/Up = `Axis $upDown-`",
            "$prefix/Down = `Axis $upDown+`",
            "$prefix/Left = `Axis $leftRight-`",
            "$prefix/Right = `Axis $leftRight+`",
        )
    }

    // Applies the same physical -> virtual remap SidewaysMapper uses for live HID output, so a
    // customized source resolves to the control name Dolphin would actually see for that body.
    private fun specFor(side: JoyconSide, physical: JoyconButton): String? {
        val virtualId = when (side) {
            JoyconSide.DUAL -> physical.id
            JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(physical.id)).first()
            JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(physical.id)).first()
        }
        val virtual = JoyconButton.entries.firstOrNull { it.id == virtualId } ?: return null
        return ANDROID_NAMES[virtual] ?: HAT_NAMES[virtual]
    }
}
