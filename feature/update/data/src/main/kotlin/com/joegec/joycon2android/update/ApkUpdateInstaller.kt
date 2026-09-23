package com.joegec.joycon2android.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class ApkUpdateInstaller(
    private val downloadDirectory: File,
    private val downloader: ApkDownloader,
    private val systemInstaller: SystemPackageInstaller,
) : UpdateInstaller {

    override fun install(update: AvailableUpdate): Flow<InstallProgress> = flow {
        if (!systemInstaller.allowedToInstall()) {
            systemInstaller.requestPermission()
            emit(InstallProgress.NeedsPermission)
            return@flow
        }

        emit(InstallProgress.Downloading(0))
        val apk = emptyApkFile(update)
        val downloaded = try {
            downloader.download(update.downloadUrl, apk) { emit(InstallProgress.Downloading(it)) }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }

        if (!downloaded) {
            apk.delete()
            emit(InstallProgress.Failed)
            return@flow
        }

        systemInstaller.install(apk)
        emit(InstallProgress.HandedOff)
    }.flowOn(Dispatchers.IO)

    // A half-written file from an interrupted download would install as a corrupt package.
    private fun emptyApkFile(update: AvailableUpdate): File {
        downloadDirectory.mkdirs()
        downloadDirectory.listFiles()?.forEach { it.delete() }
        return File(downloadDirectory, "joycon2android-${update.version}.apk")
    }
}
