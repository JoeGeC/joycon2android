package com.joegec.joycon2android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseParserTest {

    private val parser = GitHubReleaseParser()

    @Test
    fun `reads the version, highlights and apk url`() {
        val update = parser.parse(
            release(
                tag = "v1.2.0",
                body = "## What's new\\n- **Smoother motion** Gyro updates arrive faster.",
                assets = """
                    {"name": "joycon2android-1.2.0.apk", "browser_download_url": "https://example.test/app.apk"}
                """.trimIndent(),
            )
        )

        assertEquals(AppVersion(1, 2, 0), update?.version)
        assertEquals(listOf("Smoother motion"), update?.highlights)
        assertEquals("https://example.test/app.apk", update?.downloadUrl)
    }

    @Test
    fun `picks the apk out of a release that also ships other assets`() {
        val update = parser.parse(
            release(
                tag = "v2.0.0",
                assets = """
                    {"name": "mapping.json", "browser_download_url": "https://example.test/mapping.json"},
                    {"name": "joycon2android-2.0.0.apk", "browser_download_url": "https://example.test/app.apk"}
                """.trimIndent(),
            )
        )

        assertEquals("https://example.test/app.apk", update?.downloadUrl)
    }

    @Test
    fun `returns nothing when the release has no apk to install`() {
        assertNull(parser.parse(release(tag = "v1.2.0", assets = "")))
    }

    @Test
    fun `returns nothing when the tag is not a version`() {
        val json = release(
            tag = "nightly",
            assets = """{"name": "app.apk", "browser_download_url": "https://example.test/app.apk"}""",
        )

        assertNull(parser.parse(json))
    }

    private fun release(tag: String, body: String = "", assets: String) =
        """{"tag_name": "$tag", "body": "$body", "assets": [$assets]}"""
}
