package com.joegec.joycon2android.update

import kotlinx.coroutines.flow.Flow

interface UpdateInstaller {
    fun install(update: AvailableUpdate): Flow<InstallProgress>
}
