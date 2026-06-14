package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Dolphin's GCPadNew.ini mappings for the Virtual Gamepad, one `[GCPadN]` section per
 * assigned player. Each player's UHID pad shows up to Dolphin as a distinct Android input device
 * (`Android/<n>/Joy-Con Virtual Gamepad <n>`); the relay remaps buttons/sticks by orientation, so
 * the control names differ between a sideways single Joy-Con and a pair — hence a layout per body.
 * Control names are exactly what Dolphin reports for our pad (captured from a real mapping).
 */
object DolphinGcpadConfig {
    val path = "/sdcard/Android/data/${DolphinDsuConfig.PACKAGE}/files/Config/GCPadNew.ini"
    val corePath = "/sdcard/Android/data/${DolphinDsuConfig.PACKAGE}/files/Config/Dolphin.ini"

    private const val STANDARD_CONTROLLER = "6" // Dolphin SIDevice: Standard Controller

    fun merge(existing: String?, players: List<PlayerState>): String =
        DolphinIni.mergeSections(existing, sections(players))

    /** Sets each configured player's GameCube port to a Standard Controller in Dolphin.ini. */
    fun mergeCore(existing: String?, players: List<PlayerState>): String {
        val siDevices = players
            .filter { it.hasController && !it.hasPro && it.player.index in 1..4 }
            .associate { "SIDevice${it.player.index - 1}" to STANDARD_CONTROLLER }
        return DolphinIni.setKeys(existing, "[Core]", siDevices)
    }

    // Dolphin's device id is the pad's enumeration rank among active virtual gamepads (1-based),
    // which is NOT the player number when a lower slot is empty (e.g. P4 with no P3 → Android/3/…).
    // The device *name* still carries the player number. Sections stay on the player's own GC port.
    private fun sections(players: List<PlayerState>): Map<String, String> =
        players.filter { it.hasController }
            .sortedBy { it.player.index }
            .mapIndexedNotNull { rank, player ->
                val index = player.player.index
                if (index !in 1..4) return@mapIndexedNotNull null
                bodyFor(player, index, deviceId = rank + 1)?.let { "[GCPad$index]" to it }
            }.toMap()

    private fun bodyFor(player: PlayerState, index: Int, deviceId: Int): String? {
        val lines = when {
            player.hasPro -> return null
            player.hasFullController -> bothLines()
            player.right != null -> rightLines()
            player.left != null -> leftLines()
            else -> return null
        }
        val device = "Device = Android/$deviceId/Joy-Con Virtual Gamepad $index"
        return (listOf(device) + lines).joinToString("\n", postfix = "\n")
    }

    private fun bothLines() = listOf(
        "Buttons/A = `Button A`",
        "Buttons/B = `Button B`",
        "Buttons/X = `Button C`",
        "Buttons/Y = `Button X`",
        "Buttons/Z = `Button Z`",
        "Buttons/Start = `Button R2`",
        "Main Stick/Up = `Axis 14-`",
        "Main Stick/Down = `Axis 14+`",
        "Main Stick/Left = `Axis 11-`",
        "Main Stick/Right = `Axis 11+`",
        "C-Stick/Up = `Axis 1-`",
        "C-Stick/Down = `Axis 1+`",
        "C-Stick/Left = `Axis 0-`",
        "C-Stick/Right = `Axis 0+`",
        "Triggers/L = `Button Y`",
        "Triggers/R = `Button Z`",
        "D-Pad/Up = `Axis 16-`",
        "D-Pad/Down = `Axis 16+`",
        "D-Pad/Left = `Axis 15-`",
        "D-Pad/Right = `Axis 15+`",
    )

    private fun leftLines() = listOf(
        "Buttons/A = `Axis 15+`",
        "Buttons/B = `Axis 16+`",
        "Buttons/X = `Axis 16-`",
        "Buttons/Y = `Axis 15-`",
        "Buttons/Z = `Button L3`",
        "Buttons/Start = `Button L2`",
        "Main Stick/Up = `Axis 1-`",
        "Main Stick/Down = `Axis 1+`",
        "Main Stick/Left = `Axis 0-`",
        "Main Stick/Right = `Axis 0+`",
        "Triggers/L = `Button Z`",
        "Triggers/R = `Button R1`",
    )

    private fun rightLines() = listOf(
        "Buttons/A = `Button C`",
        "Buttons/B = `Button A`",
        "Buttons/X = `Button X`",
        "Buttons/Y = `Button B`",
        "Buttons/Z = `Mode`",
        "Buttons/Start = `Button R2`",
        "Main Stick/Up = `Axis 1-`",
        "Main Stick/Down = `Axis 1+`",
        "Main Stick/Left = `Axis 0-`",
        "Main Stick/Right = `Axis 0+`",
        "Triggers/L = `Button Y`",
        "Triggers/R = `Button L1`",
    )
}
