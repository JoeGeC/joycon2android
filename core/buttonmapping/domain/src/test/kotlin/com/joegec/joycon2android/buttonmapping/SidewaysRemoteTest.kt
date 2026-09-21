package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MarioKartWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SidewaysRemoteTest {

    private class Chosen(private val enabled: Boolean? = null) : SidewaysRemoteRepository {
        var cleared = false
        override fun observe(console: Console): Flow<Boolean?> = flowOf(enabled)
        override suspend fun set(console: Console, enabled: Boolean) = Unit
        override suspend fun clear(console: Console) { cleared = true }
    }

    private class StoredPreset(private val presetId: String?) : MappingPresetRepository {
        override fun observe(console: Console): Flow<String?> = flowOf(presetId)
        override suspend fun set(console: Console, presetId: String) = Unit
    }

    private class StoredMapping : ControllerMappingRepository {
        override fun observe(console: Console, side: JoyconSide): Flow<Map<String, String>> = flowOf(emptyMap())
        override suspend fun set(console: Console, side: JoyconSide, targetKey: String, sourceId: String) = Unit
        override suspend fun clear(console: Console, side: JoyconSide) = Unit
    }

    private fun observe(chosen: Boolean?, presetId: String?) = runBlocking {
        ObserveSidewaysRemoteUseCase(
            Chosen(chosen),
            ObserveMappingPresetUseCase(StoredPreset(presetId)),
        )(Console.WIIMOTE_NUNCHUK).first()
    }

    @Test
    fun `the layout decides until the user does`() {
        assertTrue(observe(chosen = null, presetId = MarioKartWiiMapping.id))
        assertEquals(false, observe(chosen = null, presetId = WiiMapping.id))
    }

    @Test
    fun `the user's switch outranks the layout, either way`() {
        assertEquals(false, observe(chosen = false, presetId = MarioKartWiiMapping.id))
        assertTrue(observe(chosen = true, presetId = WiiMapping.id))
    }

    @Test
    fun `applying a layout hands the switch back to it`() = runBlocking {
        val chosen = Chosen(enabled = true)

        ApplyMappingPresetUseCase(StoredPreset(null), StoredMapping(), chosen)(Console.WIIMOTE_NUNCHUK, WiiMapping.id)

        assertTrue(chosen.cleared)
    }
}
