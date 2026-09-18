package com.joegec.joycon2android.update

/**
 * Reduces a GitHub release body to the few lines the prompt shows: the bullets under its
 * "What's new" heading, stripped of markdown the dialog cannot render.
 */
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

// Release bullets lead with a bolded summary and follow it with a sentence of consequence. The
// prompt is a glance before an install, so it shows the summary and leaves the detail to the notes.
private fun String.boldLead(): String? {
    if (!startsWith(BOLD)) return null
    val close = indexOf(BOLD, startIndex = BOLD.length)
    return if (close == -1) null else substring(BOLD.length, close)
}

private fun String.withoutMarkdown() = replace(BOLD, "").replace("`", "").trim()

private const val BOLD = "**"
