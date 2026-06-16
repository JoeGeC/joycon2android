package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.emulatorconfig.DolphinPaths

/**
 * Dolphin's DSUClient.ini on Android. It lives in Dolphin's external data dir — writable by a
 * shell-uid process (Shizuku / wireless debugging) but not by us directly. Servers are listed
 * on the `Entries` line as `;`-separated `name:host:port` tokens; [merge] adds ours without
 * disturbing any the user already configured.
 */
object DolphinDsuConfig {
    val path = DolphinPaths.config("DSUClient.ini")

    private val entry = "Joycon2:127.0.0.1:${DsuConfig.PORT}"

    fun merge(existing: String?): String {
        if (existing.isNullOrBlank()) return canonical()

        val lines = existing.lines().toMutableList()
        val entriesIndex = lines.indexOfFirst {
            it.contains('=') && it.substringBefore('=').trim().equals("Entries", ignoreCase = true)
        }
        if (entriesIndex < 0) return canonical()

        val tokens = lines[entriesIndex].substringAfter('=')
            .split(';').map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.any { it.equals(entry, ignoreCase = true) }) return existing

        lines[entriesIndex] = "Entries = " + (tokens + entry).joinToString(";") + ";"
        return lines.joinToString("\n")
    }

    private fun canonical(): String = "[Server]\nEnabled = True\nEntries = $entry;\n"
}
