package com.joegec.joycon2android.update

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckForUpdateUseCaseTest {

    private val installed = AppVersion(1, 1, 1)

    @Test
    fun `offers a newer release`() = runBlocking {
        val newer = release(AppVersion(1, 2, 0))

        assertEquals(newer, check(latest = newer)())
    }

    @Test
    fun `stays quiet when the installed version is current`() = runBlocking {
        assertNull(check(latest = release(installed))())
    }

    @Test
    fun `stays quiet when the release is older than what is installed`() = runBlocking {
        assertNull(check(latest = release(AppVersion(1, 0, 9)))())
    }

    @Test
    fun `stays quiet when the release cannot be read`() = runBlocking {
        assertNull(check(latest = null)())
    }

    @Test
    fun `stays quiet when the installed version is unknown`() = runBlocking {
        val useCase = CheckForUpdateUseCase(
            FakeUpdates(release(AppVersion(1, 2, 0))),
            FakeSkippedVersions(),
            installedVersion = null,
        )

        assertNull(useCase())
    }

    @Test
    fun `stays quiet about a skipped version`() = runBlocking {
        assertNull(check(latest = release(AppVersion(1, 2, 0)), skipped = AppVersion(1, 2, 0))())
    }

    @Test
    fun `offers a release newer than the skipped one`() = runBlocking {
        val newer = release(AppVersion(1, 3, 0))

        assertEquals(newer, check(latest = newer, skipped = AppVersion(1, 2, 0))())
    }

    private fun check(latest: AvailableUpdate?, skipped: AppVersion? = null) =
        CheckForUpdateUseCase(FakeUpdates(latest), FakeSkippedVersions(skipped), installed)

    private fun release(version: AppVersion) =
        AvailableUpdate(version, listOf("Something new"), "https://example.test/app.apk")

    private class FakeUpdates(private val latest: AvailableUpdate?) : UpdateRepository {
        override suspend fun latestRelease() = latest
    }

    private class FakeSkippedVersions(private var version: AppVersion? = null) : SkippedVersionRepository {
        override suspend fun skippedVersion() = version
        override suspend fun skip(version: AppVersion) {
            this.version = version
        }
    }
}
