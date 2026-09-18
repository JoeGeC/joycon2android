package com.joegec.joycon2android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun `parses a release tag`() {
        assertEquals(AppVersion(1, 2, 3), AppVersion.parse("v1.2.3"))
    }

    @Test
    fun `parses a bare version name`() {
        assertEquals(AppVersion(1, 1, 1), AppVersion.parse("1.1.1"))
    }

    @Test
    fun `drops a prerelease suffix`() {
        assertEquals(AppVersion(1, 2, 0), AppVersion.parse("v1.2.0-debug.47"))
    }

    @Test
    fun `rejects anything that is not three numbers`() {
        assertNull(AppVersion.parse("1.2"))
        assertNull(AppVersion.parse("1.2.3.4"))
        assertNull(AppVersion.parse("latest"))
        assertNull(AppVersion.parse(""))
    }

    @Test
    fun `orders numerically rather than alphabetically`() {
        assertTrue(AppVersion(1, 10, 0) > AppVersion(1, 9, 0))
        assertTrue(AppVersion(2, 0, 0) > AppVersion(1, 99, 99))
        assertTrue(AppVersion(1, 1, 2) > AppVersion(1, 1, 1))
    }
}
