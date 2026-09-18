package com.joegec.joycon2android.update

data class AppVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int =
        compareValuesBy(this, other, AppVersion::major, AppVersion::minor, AppVersion::patch)

    override fun toString() = "$major.$minor.$patch"

    companion object {
        // Releases are tagged vX.Y.Z; prerelease tags add a -debug.N suffix that carries no ordering.
        fun parse(raw: String): AppVersion? {
            val parts = raw.trim().removePrefix("v").substringBefore('-').split('.')
            if (parts.size != PART_COUNT) return null
            val (major, minor, patch) = parts.map { it.toIntOrNull() ?: return null }
            return AppVersion(major, minor, patch)
        }

        private const val PART_COUNT = 3
    }
}
