package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.emulatorconfig.DolphinPaths

/** `Entries` lists servers as `;`-separated `name:host:port`; [merge] keeps the user's own. */
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
