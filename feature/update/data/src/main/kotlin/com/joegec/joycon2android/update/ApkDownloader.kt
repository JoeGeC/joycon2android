package com.joegec.joycon2android.update

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Blocking by design — [ApkUpdateInstaller] runs it on an IO dispatcher. */
class ApkDownloader {

    suspend fun download(url: String, destination: File, onProgress: suspend (Int) -> Unit) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Download returned ${connection.responseCode}")
            }
            connection.copyToFile(destination, onProgress)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun HttpURLConnection.copyToFile(destination: File, onProgress: suspend (Int) -> Unit) {
        val total = contentLength.toLong()
        var written = 0L
        var reported = 0
        destination.outputStream().use { output ->
            inputStream.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    written += read
                    val percent = percentOf(written, total)
                    if (percent > reported) {
                        reported = percent
                        onProgress(percent)
                    }
                }
            }
        }
    }

    // A server that omits Content-Length leaves the bar at zero rather than jumping around.
    private fun percentOf(written: Long, total: Long) =
        if (total <= 0) 0 else ((written * PERCENT) / total).toInt()

    private companion object {
        const val TIMEOUT_MS = 30_000
        const val PERCENT = 100
    }
}
