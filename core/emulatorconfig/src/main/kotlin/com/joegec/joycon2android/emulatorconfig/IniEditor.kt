package com.joegec.joycon2android.emulatorconfig

/** Splices keys into an emulator's ini, leaving the user's other keys and sections intact. */
object IniEditor {
    fun mergeSections(existing: String?, sections: Map<String, String>): String {
        val bodies = LinkedHashMap<String, String>()
        val preamble = StringBuilder()
        var current: String? = null
        existing?.lines()?.forEach { line ->
            val header = line.trim()
            if (header.startsWith("[") && header.endsWith("]")) {
                current = header
                bodies.getOrPut(header) { "" }
            } else if (current == null) {
                if (line.isNotBlank()) preamble.append(line).append("\n")
            } else {
                bodies[current!!] = bodies.getValue(current!!) + line + "\n"
            }
        }
        bodies.putAll(sections)

        val out = StringBuilder(preamble)
        bodies.forEach { (header, body) -> out.append(header).append("\n").append(body) }
        return out.toString()
    }

    fun valueOf(existing: String?, section: String, key: String): String? {
        val lines = existing?.lines() ?: return null
        val headerIndex = lines.indexOfFirst { it.trim() == section }
        if (headerIndex < 0) return null
        for (i in headerIndex + 1 until lines.size) {
            val trimmed = lines[i].trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) return null
            if (!trimmed.contains('=')) continue
            if (trimmed.substringBefore('=').trim() == key) return trimmed.substringAfter('=').trim()
        }
        return null
    }

    fun removeKeys(existing: String?, section: String, keyMatches: (String) -> Boolean): String {
        if (existing == null) return ""
        val lines = existing.lines()
        val headerIndex = lines.indexOfFirst { it.trim() == section }
        if (headerIndex < 0) return existing

        var end = lines.size
        for (i in headerIndex + 1 until lines.size) {
            val trimmed = lines[i].trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                end = i
                break
            }
        }

        val kept = lines.subList(headerIndex + 1, end).filterNot {
            it.contains('=') && keyMatches(it.substringBefore('=').trim())
        }
        return (lines.subList(0, headerIndex + 1) + kept + lines.subList(end, lines.size)).joinToString("\n")
    }

    /** Merges keys into one section rather than replacing it, for shared files like Dolphin.ini. */
    fun setKeys(
        existing: String?,
        section: String,
        keys: Map<String, String>,
        assign: String = " = ",
    ): String {
        if (keys.isEmpty()) return existing ?: ""
        val lines = (existing?.lines() ?: emptyList()).toMutableList()

        val headerIndex = lines.indexOfFirst { it.trim() == section }
        if (headerIndex < 0) {
            if (lines.isNotEmpty() && lines.last().isNotBlank()) lines.add("")
            lines.add(section)
            keys.forEach { (key, value) -> lines.add("$key$assign$value") }
            return lines.joinToString("\n")
        }

        var end = lines.size
        for (i in headerIndex + 1 until lines.size) {
            val trimmed = lines[i].trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                end = i
                break
            }
        }

        val remaining = keys.toMutableMap()
        for (i in headerIndex + 1 until end) {
            if (!lines[i].contains('=')) continue
            val key = lines[i].substringBefore('=').trim()
            remaining.remove(key)?.let { lines[i] = "$key$assign$it" }
        }
        if (remaining.isNotEmpty()) {
            lines.addAll(end, remaining.map { (key, value) -> "$key$assign$value" })
        }
        return lines.joinToString("\n")
    }
}
