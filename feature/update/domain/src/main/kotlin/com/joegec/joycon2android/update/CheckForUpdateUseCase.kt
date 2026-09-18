package com.joegec.joycon2android.update

class CheckForUpdateUseCase(
    private val updates: UpdateRepository,
    private val skippedVersions: SkippedVersionRepository,
    private val installedVersion: AppVersion?,
) {
    suspend operator fun invoke(): AvailableUpdate? {
        val installed = installedVersion ?: return null
        val latest = updates.latestRelease() ?: return null
        if (latest.version <= installed) return null
        val skipped = skippedVersions.skippedVersion()
        return latest.takeIf { skipped == null || it.version > skipped }
    }
}
