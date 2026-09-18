package com.joegec.joycon2android.update

import kotlinx.coroutines.flow.Flow

class InstallUpdateUseCase(private val installer: UpdateInstaller) {
    operator fun invoke(update: AvailableUpdate): Flow<InstallProgress> = installer.install(update)
}
