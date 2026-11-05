package com.joegec.joycon2android.output

import com.joegec.joycon2android.output.OutputMode.GAMEPAD
import com.joegec.joycon2android.output.OutputMode.MOTION
import com.joegec.joycon2android.output.OutputMode.MOUSE_KEYBOARD
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayingMigrationTest {

    private val eden = EmulatorTarget("dev.eden.eden_emulator", "dev.eden.eden_emulator", "Eden", EmulatorKind.EDEN)
    private val nightly = EmulatorTarget("dev.eden.eden_emulator.nightly", "dev.eden.eden_emulator.nightly", "Eden Nightly", EmulatorKind.EDEN)
    private val wii = EmulatorTarget("dolphin-wii", "org.dolphinemu.dolphinemu", "Dolphin (Wii)", EmulatorKind.DOLPHIN_WII)
    private val gameCube = EmulatorTarget("dolphin-gamecube", "org.dolphinemu.dolphinemu", "Dolphin (GameCube)", EmulatorKind.DOLPHIN_GAMECUBE)

    private fun migrate(
        remembered: Set<OutputMode>?,
        gamepad: EmulatorTarget? = EmulatorTarget.OTHER,
        motion: EmulatorTarget? = EmulatorTarget.OTHER,
        inView: OutputMode = GAMEPAD,
    ) = PlayingMigration.selection(remembered, inView) { mode -> if (mode == GAMEPAD) gamepad else motion }

    @Test
    fun `Mouse & keys running becomes PC games, whatever else ran`() {
        assertEquals(Playing.PC, migrate(setOf(MOUSE_KEYBOARD, MOTION), motion = eden).playing)
    }

    @Test
    fun `Eden on Motion becomes Switch with motion, on the gamepad alone without`() {
        assertEquals(PlayingSelection(Playing.SWITCH, mapOf(Playing.SWITCH to eden.id), motion = true), migrate(setOf(MOTION), motion = eden))
        assertEquals(PlayingSelection(Playing.SWITCH, mapOf(Playing.SWITCH to nightly.id), motion = false), migrate(setOf(GAMEPAD), gamepad = nightly))
    }

    @Test
    fun `Dolphin becomes Wii from Motion and GameCube from the gamepad, Motion first`() {
        assertEquals(Playing.WII, migrate(setOf(MOTION, GAMEPAD), gamepad = gameCube, motion = wii).playing)
        assertEquals(Playing.GAMECUBE, migrate(setOf(GAMEPAD), gamepad = gameCube, motion = wii).playing)
    }

    @Test
    fun `Other becomes Other emulator on Motion and Android on the gamepad`() {
        assertEquals(Playing.OTHER_EMULATOR, migrate(setOf(MOTION)).playing)
        assertEquals(Playing.ANDROID, migrate(setOf(GAMEPAD)).playing)
    }

    @Test
    fun `with nothing running the tab in view decides, and the card starts off`() {
        assertEquals(Playing.WII, migrate(emptySet(), motion = wii, inView = MOTION).playing)
        assertFalse(PlayingMigration.wasOn(emptySet()))
    }

    @Test
    fun `never stored, the tab in view ran, so the card starts on`() {
        assertEquals(Playing.PC, migrate(null, inView = MOUSE_KEYBOARD).playing)
        assertTrue(PlayingMigration.wasOn(null))
    }

    @Test
    fun `an Eden pick carries over as the Switch emulator`() {
        assertEquals(mapOf(Playing.SWITCH to eden.id), migrate(setOf(GAMEPAD), gamepad = eden, motion = wii).emulators)
        assertEquals(Playing.SWITCH, migrate(setOf(GAMEPAD), gamepad = eden, motion = wii).playing)
    }
}
