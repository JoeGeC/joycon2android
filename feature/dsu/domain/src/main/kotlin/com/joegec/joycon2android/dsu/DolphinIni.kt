package com.joegec.joycon2android.dsu

/** Replaces the given `[Section]` blocks in an existing ini, leaving every other section intact. */
internal object DolphinIni {
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

    /**
     * Sets `key = value` entries inside a single `[section]`, replacing matching keys and appending
     * the rest, while leaving every other key and section intact. Used for shared files like
     * Dolphin.ini where wholesale section replacement would wipe unrelated settings.
     */
    fun setKeys(existing: String?, section: String, keys: Map<String, String>): String {
        if (keys.isEmpty()) return existing ?: ""
        val lines = (existing?.lines() ?: emptyList()).toMutableList()

        val headerIndex = lines.indexOfFirst { it.trim() == section }
        if (headerIndex < 0) {
            if (lines.isNotEmpty() && lines.last().isNotBlank()) lines.add("")
            lines.add(section)
            keys.forEach { (key, value) -> lines.add("$key = $value") }
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
            remaining.remove(key)?.let { lines[i] = "$key = $it" }
        }
        if (remaining.isNotEmpty()) {
            lines.addAll(end, remaining.map { (key, value) -> "$key = $value" })
        }
        return lines.joinToString("\n")
    }
}
