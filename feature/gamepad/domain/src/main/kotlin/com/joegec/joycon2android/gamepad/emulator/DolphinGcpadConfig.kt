package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Dolphin's GCPadNew.ini mappings for the Virtual Gamepad, one `[GCPadN]` section per
 * assigned player, driven by the user's customizable Joy-Con -> GameCube mapping. Each player's
 * UHID pad shows up to Dolphin as a distinct Android input device
 * (`Android/<controllerNumber>/Joy-Con Virtual Gamepad <player>`); the relay remaps buttons/sticks
 * by orientation (see [SidewaysMapper]), so which physical button reaches a given Android control differs
 * between a sideways single Joy-Con and a pair. [ANDROID_NAMES]/[HAT_NAMES] are the fixed,
 * body-independent Dolphin names for each Android keycode/hat direction our virtual pad emits
 * (captured from a real mapping); [specFor] resolves a customized source to the one its body
 * actually emits. Every stick direction is its own Dolphin input, so a stick target can mix
 * stick tilts and buttons freely without losing analog range on the tilts.
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
    // GR and Chat are absent: they land on BUTTON_1/BUTTON_2, and a GameCube pad has no target
    // left for them anyway.
    private val ANDROID_NAMES = mapOf(
        JoyconButton.A to "Button A",
        JoyconButton.B to "Button B",
        JoyconButton.Camera to "Button C",
        JoyconButton.X to "Button X",
        JoyconButton.Y to "Button Y",
        JoyconButton.GL to "Button Z",
        JoyconButton.L to "Button L1",
        JoyconButton.R to "Button R1",
        JoyconButton.ZL to "Button L2",
        JoyconButton.ZR to "Button R2",
        JoyconButton.LS to "Button L3",
        JoyconButton.RS to "Button R3",
        JoyconButton.Plus to "Start",
        JoyconButton.Minus to "Select",
        JoyconButton.Home to "Mode",
    )

    // The D-Pad target reads the hat switch instead: Axis 15 = hat X, Axis 16 = hat Y.
    private val HAT_NAMES = mapOf(
        JoyconButton.Up to "Axis 16-",
        JoyconButton.Down to "Axis 16+",
        JoyconButton.Left to "Axis 15-",
        JoyconButton.Right to "Axis 15+",
    )

    private val STICK_PREFIXES = mapOf(GameCubeStick.MainStick to "Main Stick", GameCubeStick.CStick to "C-Stick")

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        controllerNumbers: Map<Int, Int>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): String = IniEditor.mergeSections(existing, sections(players, controllerNumbers, mappingFor))

    /** Sets each configured player's GameCube port to a Standard Controller in Dolphin.ini. */
    fun mergeCore(existing: String?, players: List<PlayerState>): String {
        val siDevices = players
            .filter { it.hasController && !it.hasPro && it.player.index in 1..4 }
            .associate { "SIDevice${it.player.index - 1}" to STANDARD_CONTROLLER }
        return IniEditor.setKeys(existing, "[Core]", siDevices)
    }

    // Dolphin's device id comes from Android's own gamepad enumeration counter
    // (InputDevice.getControllerNumber()), so it has to be read from the live device list rather
    // than derived — any built-in controller already holds number 1. A player whose pad isn't
    // enumerated yet is skipped: a guessed id binds the section to the wrong device, or to none.
    private fun sections(
        players: List<PlayerState>,
        controllerNumbers: Map<Int, Int>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): Map<String, String> =
        players.filter { it.hasController }
            .sortedBy { it.player.index }
            .mapNotNull { player ->
                val index = player.player.index
                if (index !in 1..4) return@mapNotNull null
                val deviceId = controllerNumbers[index] ?: return@mapNotNull null
                bodyFor(player, index, deviceId, mappingFor)?.let { "[GCPad$index]" to it }
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
        val buttonLines = mapping.toSourceMap<GameCubeButton>().mapNotNull { (target, source) ->
            specFor(side, source)?.let { spec -> "${DOLPHIN_KEYS.getValue(target)} = `$spec`" }
        }
        val stickLines = mapping.toStickDirectionMap<GameCubeStick>().flatMap { (target, directions) ->
            directions.mapNotNull { (direction, source) ->
                specFor(side, source)?.let { spec -> "${STICK_PREFIXES.getValue(target)}/${direction.displayName} = `$spec`" }
            }
        }
        return buttonLines + stickLines
    }

    private fun specFor(side: JoyconSide, source: MappingSource): String? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side)?.let { ANDROID_NAMES[it] ?: HAT_NAMES[it] }
        is MappingSource.Stick -> tiltSpec(source.emittedStick(side), source.direction)
    }

    // Physical left stick lands on Android axes 0/1, physical right stick on axes 11/14 (see
    // ReportMapper); Android's Y axis grows downward.
    private fun tiltSpec(stick: StickSource, direction: StickDirection): String {
        val (x, y) = if (stick == StickSource.LEFT_STICK) 0 to 1 else 11 to 14
        return when (direction) {
            StickDirection.UP -> "Axis $y-"
            StickDirection.DOWN -> "Axis $y+"
            StickDirection.LEFT -> "Axis $x-"
            StickDirection.RIGHT -> "Axis $x+"
        }
    }
}
