package com.joegec.joycon2android.update

/** The bullets under "What's new", stripped of markdown the dialog can't render. */
object ReleaseNotes {

    fun highlights(body: String): List<String> {
        val lines = body.lines().map(String::trim)
        return lines.whatsNewSection().ifEmpty { lines }
            .filter(String::isBullet)
            .map(String::asHighlight)
            .filter(String::isNotEmpty)
            .take(MAX_HIGHLIGHTS)
    }

    private const val MAX_HIGHLIGHTS = 6
}

private fun List<String>.whatsNewSection(): List<String> {
    val heading = indexOfFirst {
        it.isHeading() && it.contains("what", ignoreCase = true) && it.contains("new", ignoreCase = true)
    }
    if (heading == -1) return emptyList()
    return drop(heading + 1).takeWhile { !it.isHeading() }
}

private fun String.isHeading() = startsWith("#")

private fun String.isBullet() = startsWith("- ") || startsWith("* ")

private fun String.asHighlight(): String {
    val bullet = drop(2).trim()
    return (bullet.boldLead() ?: bullet).withoutMarkdown()
}

// A bullet opens with a bold summary; the prompt shows only that.
private fun String.boldLead(): String? {
    if (!startsWith(BOLD)) return null
    val close = indexOf(BOLD, startIndex = BOLD.length)
    return if (close == -1) null else substring(BOLD.length, close)
}

private fun String.withoutMarkdown() = replace(BOLD, "").replace("`", "").trim()

private const val BOLD = "**"
