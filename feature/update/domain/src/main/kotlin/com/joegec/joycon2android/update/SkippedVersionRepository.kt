package com.joegec.joycon2android.update

interface SkippedVersionRepository {
    suspend fun skippedVersion(): AppVersion?
    suspend fun skip(version: AppVersion)
}
