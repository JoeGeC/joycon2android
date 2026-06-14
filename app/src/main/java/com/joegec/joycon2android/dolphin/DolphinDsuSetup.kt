package com.joegec.joycon2android.dolphin

import android.content.pm.PackageManager
import com.joegec.joycon2android.dsu.DolphinDsuConfig
import com.joegec.joycon2android.dsu.DolphinWiimoteConfig
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.uhid.PrivilegedShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Writes Dolphin's DSUClient.ini through the privileged shell (Shizuku / wireless debugging),
 * merging our server entry into whatever is already there. Best-effort: returns false — and the
 * UI falls back to the manual copy steps — when no shell is available or the write can't be
 * verified, since some OEM builds deny even the shell user access to another app's Android/data.
 */
class DolphinDsuSetup(
    private val packageManager: PackageManager,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
) {
    val dolphinInstalled: Boolean
        get() = runCatching { packageManager.getPackageInfo(DolphinDsuConfig.PACKAGE, 0) }.isSuccess

    suspend fun configure(players: List<PlayerState>): Boolean = withContext(Dispatchers.IO) {
        val shell = awaitShell() ?: return@withContext false

        val dsuMerged = DolphinDsuConfig.merge(shell.readText(DolphinDsuConfig.path))
        shell.writeText(DolphinDsuConfig.path, dsuMerged)
        val dsuOk = shell.readText(DolphinDsuConfig.path)?.contains("127.0.0.1:${DsuConfig.PORT}") == true

        val configurable = players.any {
            !it.hasPro && (it.left != null || it.right != null) && it.player.index in 1..4
        }
        val wiimoteOk = !configurable || shell.writeText(
            DolphinWiimoteConfig.path,
            DolphinWiimoteConfig.merge(shell.readText(DolphinWiimoteConfig.path), players),
        )

        dsuOk && wiimoteOk
    }

    private suspend fun awaitShell(): PrivilegedShell? =
        suspendCancellableCoroutine { cont -> acquireShell { cont.resume(it) } }
}

private fun PrivilegedShell.readText(path: String): String? {
    val proc = shell("cat '$path' 2>/dev/null") ?: return null
    return try {
        proc.inputStream.readBytes().decodeToString().also { proc.waitFor() }
    } catch (_: Exception) {
        null
    } finally {
        proc.destroy()
    }
}

private fun PrivilegedShell.writeText(path: String, content: String): Boolean {
    val dir = path.substringBeforeLast('/')
    val proc = shell("mkdir -p '$dir' && cat > '$path'") ?: return false
    return try {
        proc.outputStream.use { it.write(content.encodeToByteArray()) }
        proc.waitFor()
        true
    } catch (_: Exception) {
        false
    } finally {
        proc.destroy()
    }
}
