package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidKeyMappingTest {

    private val p1 = PlayerBody(PlayerNumber.P1, JoyconSide.DUAL)
    private val p2 = PlayerBody(PlayerNumber.P2, JoyconSide.LEFT)

    @Test
    fun `an Android key fires on any of its buttons and ignores stick directions`() {
        val entries = mapOf(AndroidKey.HOME.targetKey to "Home|Chat|LEFT_STICK_UP")

        assertEquals(
            mapOf(AndroidKey.HOME to setOf(JoyconButton.Home, JoyconButton.Chat)),
            entries.androidKeyBindings().buttons,
        )
    }

    @Test
    fun `each player and console keeps its own Android keys`() = runBlocking {
        val mappings = FakeControllerMappings()
        mappings.set(Console.GAMECUBE, p1, AndroidKey.HOME.targetKey, "Home")
        mappings.set(Console.GAMECUBE, p2, AndroidKey.BACK.targetKey, "Minus")
        mappings.set(Console.SWITCH_PRO, p1, AndroidKey.SCREENSHOT.targetKey, "Capture")

        val bindings = ObserveAndroidKeyBindingsUseCase(ObserveControllerMappingUseCase(mappings))(
            Console.GAMECUBE,
            listOf(p1, p2),
        ).first()

        assertEquals(mapOf(AndroidKey.HOME to setOf(JoyconButton.Home)), bindings.getValue(PlayerNumber.P1).buttons)
        assertEquals(mapOf(AndroidKey.BACK to setOf(JoyconButton.Minus)), bindings.getValue(PlayerNumber.P2).buttons)
    }

    @Test
    fun `an Android key cleared back to None still matches the layout`() {
        val entries = GameCubeMapping.entries(JoyconSide.DUAL) + (AndroidKey.HOME.targetKey to "")

        val layout = MappingLayouts.matching(Console.GAMECUBE, JoyconSide.DUAL, entries, false, emptyList())

        assertEquals(GameCubeMapping, layout)
    }

    @Test
    fun `a bound Android key makes the mapping custom`() {
        val entries = GameCubeMapping.entries(JoyconSide.DUAL) + (AndroidKey.HOME.targetKey to "Home")

        assertEquals(null, MappingLayouts.matching(Console.GAMECUBE, JoyconSide.DUAL, entries, false, emptyList()))
    }
}
