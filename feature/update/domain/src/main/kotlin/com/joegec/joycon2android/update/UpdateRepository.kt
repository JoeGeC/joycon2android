package com.joegec.joycon2android.update

interface UpdateRepository {
    /** The newest published release, or null when it cannot be read — offline, rate limited, malformed. */
    suspend fun latestRelease(): AvailableUpdate?
}
