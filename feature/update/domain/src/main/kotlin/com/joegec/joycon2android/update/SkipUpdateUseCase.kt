package com.joegec.joycon2android.update

class SkipUpdateUseCase(private val skippedVersions: SkippedVersionRepository) {
    suspend operator fun invoke(update: AvailableUpdate) = skippedVersions.skip(update.version)
}
