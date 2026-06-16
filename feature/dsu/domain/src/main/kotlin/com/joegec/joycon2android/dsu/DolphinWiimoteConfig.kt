package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Dolphin's WiimoteNew.ini button mappings for the DSU device, one `[WiimoteN]`
 * section per assigned player (player N streams on DSU slot N-1 → `DSUClient/<slot>/Joycon2`,
 * the name matching our [DolphinDsuConfig] entry). The mapping set depends on what the player
 * holds: a single sideways Joy-Con drives the D-pad from its analog stick, a pair drives it
 * from the physical D-pad and exposes the left stick as the Nunchuk. Control names are the
 * DS4-convention names the DSU device exposes (see the in-app mapping table).
 */
object DolphinWiimoteConfig {
    val path = DolphinPaths.config("WiimoteNew.ini")

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

    private val stickDPad = listOf(
        "D-Pad/Up = `Left Y+`",
        "D-Pad/Down = `Left Y-`",
        "D-Pad/Left = `Left X-`",
        "D-Pad/Right = `Left X+`",
    )

    fun merge(existing: String?, players: List<PlayerState>): String =
        IniEditor.mergeSections(existing, sections(players))

    private fun sections(players: List<PlayerState>): Map<String, String> =
        players.mapNotNull { player ->
            val slot = player.player.index - 1
            if (slot !in 0..3) return@mapNotNull null
            bodyFor(player, slot)?.let { "[Wiimote${player.player.index}]" to it }
        }.toMap()

    private fun bodyFor(player: PlayerState, slot: Int): String? {
        val lines = when {
            player.hasPro -> return null
            player.hasFullController -> bothLines()
            player.right != null -> rightLines()
            player.left != null -> leftLines()
            else -> return null
        }
        // Source = 1 forces this Wii Remote slot to Emulated, so the mappings actually apply
        val header = listOf("Source = 1", "Device = DSUClient/$slot/Joycon2")
        return (header + lines + imuLines).joinToString("\n", postfix = "\n")
    }

    private fun rightLines() = listOf(
        "Buttons/A = `Circle`",
        "Buttons/B = `R2`",
        "Buttons/1 = `Square`",
        "Buttons/2 = `Cross`",
        "Buttons/Home = `PS`",
        "Buttons/+ = `Options`",
        "Buttons/- = `Triangle`",
        "IMUIR/Recenter = `R1`",
        "Extension = None",
    ) + stickDPad

    private fun leftLines() = listOf(
        "Buttons/A = `Pad E`",
        "Buttons/B = `L2`",
        "Buttons/1 = `Pad W`",
        "Buttons/2 = `Pad S`",
        "Buttons/Home = `Touch`",
        "Buttons/+ = `Pad N`",
        "Buttons/- = `Share`",
        "IMUIR/Recenter = `L1`",
        "Extension = None",
    ) + stickDPad

    private fun bothLines() = listOf(
        "Buttons/A = `Circle`",
        "Buttons/B = `R2`",
        "Buttons/1 = `Square`",
        "Buttons/2 = `Cross`",
        "Buttons/Home = `PS`",
        "Buttons/+ = `Options`",
        "Buttons/- = `Triangle`",
        "D-Pad/Up = `Pad N`",
        "D-Pad/Down = `Pad S`",
        "D-Pad/Left = `Pad W`",
        "D-Pad/Right = `Pad E`",
        "IMUIR/Recenter = `R1`",
        "Extension = Nunchuk",
        "Nunchuk/Buttons/C = `L1`",
        "Nunchuk/Buttons/Z = `L2`",
        "Nunchuk/Stick/Up = `Left Y+`",
        "Nunchuk/Stick/Down = `Left Y-`",
        "Nunchuk/Stick/Left = `Left X-`",
        "Nunchuk/Stick/Right = `Left X+`",
    )
}
