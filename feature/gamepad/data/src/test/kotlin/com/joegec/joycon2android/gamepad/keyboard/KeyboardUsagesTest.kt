package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.KeyboardKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KeyboardUsagesTest {

    @Test
    fun `every key is either a usage or a modifier`() {
        KeyboardKey.entries.forEach { key ->
            assertNotNull(key.name, KeyboardUsages.usage(key) ?: KeyboardUsages.modifierBit(key))
        }
    }

    @Test
    fun `the device without letters still declares every key that is not one`() {
        KeyboardKey.entries.forEach { key ->
            val usage = KeyboardUsages.usage(key) ?: return@forEach
            assertEquals(key.name, key.isLetter, usage < KeyboardUsages.FIRST_NON_LETTER)
        }
    }

    @Test
    fun `no two keys share a usage`() {
        val usages = KeyboardKey.entries.mapNotNull(KeyboardUsages::usage)

        assertEquals(usages.size, usages.toSet().size)
    }
}
