package com.joegec.joycon2android.update

import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseNotesTest {

    @Test
    fun `takes the bolded lead of each bullet under What's new`() {
        val body = """
            Motion feels smoother and setup is one tap less.

            ## What's new
            - **Faster motion updates** Games that read gyro now feel smoother.
            - **One-tap Dolphin setup** The config is written for you.

            ## Install
            - Download joycon2android-1.2.0.apk
        """.trimIndent()

        assertEquals(
            listOf("Faster motion updates", "One-tap Dolphin setup"),
            ReleaseNotes.highlights(body),
        )
    }

    @Test
    fun `falls back to every bullet when there is no What's new heading`() {
        val body = """
            A quick fix release.

            - Reconnects after the screen sleeps
            * Battery reads correctly at 100%
        """.trimIndent()

        assertEquals(
            listOf("Reconnects after the screen sleeps", "Battery reads correctly at 100%"),
            ReleaseNotes.highlights(body),
        )
    }

    @Test
    fun `keeps the whole bullet when it has no bolded lead`() {
        val body = "## What's new\n- Reconnects after the screen sleeps"

        assertEquals(listOf("Reconnects after the screen sleeps"), ReleaseNotes.highlights(body))
    }

    @Test
    fun `is empty when the body has no bullets at all`() {
        assertEquals(emptyList<String>(), ReleaseNotes.highlights("Just a sentence."))
    }
}
