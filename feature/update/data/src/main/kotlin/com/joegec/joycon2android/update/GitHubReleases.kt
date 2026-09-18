package com.joegec.joycon2android.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The `releases/latest` endpoint never returns a prerelease, so the `-debug.N` builds cut for
 * sharing are invisible here. Unauthenticated calls are rate limited to 60 an hour per address;
 * exceeding it reads as "no update", like being offline.
 */
class GitHubReleases(
    private val repository: String,
    private val parser: GitHubReleaseParser = GitHubReleaseParser(),
) : UpdateRepository {

    override suspend fun latestRelease(): AvailableUpdate? = withContext(Dispatchers.IO) {
        try {
            parser.parse(fetchLatest())
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchLatest(): String {
        val connection = (URL(LATEST_RELEASE_URL.format(repository)).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", ACCEPT_JSON)
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub returned ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/%s/releases/latest"
        const val ACCEPT_JSON = "application/vnd.github+json"
        const val TIMEOUT_MS = 10_000
    }
}
