package com.joegec.joycon2android.emulator

import android.content.pm.PackageManager
import android.util.Log
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.dsu.emulator.DolphinDsuConfig
import com.joegec.joycon2android.dsu.emulator.DolphinWiimoteConfig
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.gamepad.emulator.DolphinGcpadConfig
import com.joegec.joycon2android.gamepad.emulator.EdenGamepadConfig
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.ui.components.EmulatorOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Writes emulator config to match the current player assignment, through the privileged shell
 * (Shizuku / wireless debugging). Best-effort: returns false — and the UI falls back to manual
 * setup — when no shell is available or the write can't be verified, since some OEM builds deny
 * even the shell user access to another app's Android/data.
 */
class EmulatorSetup(
    private val packageManager: PackageManager,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
    private val scope: CoroutineScope,
    private val gamepadPorts: () -> Map<Int, Int>,
    private val getControllerMapping: GetEffectiveControllerMappingUseCase,
) {

    private suspend fun mappingLookup(console: Console): (JoyconSide) -> Map<String, String> {
        val bySide = JoyconSide.entries.associateWith { getControllerMapping(console, it) }
        return { side -> bySide.getValue(side) }
    }
    val dolphinInstalled: Boolean
        get() = isInstalled(DolphinPaths.PACKAGE)

    /** Installed emulators whose controller mapping the Virtual Gamepad can configure. */
    fun gamepadEmulators(): List<EmulatorOption> = buildList {
        if (isInstalled(DolphinPaths.PACKAGE)) {
            add(EmulatorOption(DolphinPaths.PACKAGE, "Dolphin (GameCube)"))
        }
        if (isInstalled(EdenGamepadConfig.PACKAGE)) {
            add(EmulatorOption(EdenGamepadConfig.PACKAGE, "Eden"))
        }
    }

    private fun isInstalled(pkg: String) =
        runCatching { packageManager.getPackageInfo(pkg, 0) }.isSuccess

    /** Dolphin DSU + Wii Remote mappings (DSU card). */
    suspend fun configureDolphinDsu(players: List<PlayerState>): EmulatorSetupResult = bounded("dsu") {
        val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS

        val dsuMerged = DolphinDsuConfig.merge(shell.readText(DolphinDsuConfig.path))
        shell.writeText(DolphinDsuConfig.path, dsuMerged)
        val dsuOk = shell.readText(DolphinDsuConfig.path)?.contains("127.0.0.1:${DsuConfig.PORT}") == true

        val configurable = players.any {
            !it.hasPro && (it.left != null || it.right != null) && it.player.index in 1..4
        }
        val wiimoteOk = !configurable || shell.writeText(
            DolphinWiimoteConfig.path,
            DolphinWiimoteConfig.merge(
                shell.readText(DolphinWiimoteConfig.path),
                players,
                mappingLookup(Console.WIIMOTE_NUNCHUK),
            ),
        )

        if (dsuOk && wiimoteOk) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
    }

    /** Controller mapping for the selected emulator (Gamepad card). */
    suspend fun configureGamepad(emulatorId: String, players: List<PlayerState>): EmulatorSetupResult =
        bounded("gamepad") {
            val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
            val written = when (emulatorId) {
                EdenGamepadConfig.PACKAGE -> shell.writeText(
                    EdenGamepadConfig.path,
                    EdenGamepadConfig.merge(
                        shell.readText(EdenGamepadConfig.path),
                        players,
                        gamepadPorts(),
                        mappingLookup(Console.SWITCH_PRO),
                    ),
                )
                else -> {
                    val mappings = DolphinGcpadConfig.merge(
                        shell.readText(DolphinGcpadConfig.path),
                        players,
                        mappingLookup(Console.GAMECUBE),
                    )
                    val mappingsOk = shell.writeText(DolphinGcpadConfig.path, mappings)
                    // Dolphin GC ports default to "None"; set them to Standard Controller
                    val core = DolphinGcpadConfig.mergeCore(shell.readText(DolphinGcpadConfig.corePath), players)
                    val coreOk = shell.writeText(DolphinGcpadConfig.corePath, core)
                    mappingsOk && coreOk
                }
            }
            if (written) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
        }

    // Shell reads/writes block on native binder/socket calls that coroutine cancellation can't
    // interrupt, so run them on a scope that outlives the wait and abandon it on timeout — that
    // way the "Setting up…" spinner always resolves instead of pinning if a call never returns.
    private suspend fun bounded(tag: String, block: suspend () -> EmulatorSetupResult): EmulatorSetupResult {
        val work = scope.async(Dispatchers.IO) {
            runCatching { block() }
                .onFailure { Log.w(TAG, "$tag config threw", it) }
                .getOrDefault(EmulatorSetupResult.FAILED)
        }
        val result = withTimeoutOrNull(OPERATION_TIMEOUT_MS) { work.await() }
        if (result == null) {
            Log.w(TAG, "$tag config timed out after ${OPERATION_TIMEOUT_MS}ms")
            work.cancel()
        }
        val outcome = result ?: EmulatorSetupResult.FAILED
        Log.i(TAG, "$tag config -> $outcome")
        return outcome
    }

    private suspend fun awaitShell(): PrivilegedShell? =
        suspendCancellableCoroutine { cont ->
            acquireShell { shell ->
                Log.i(TAG, "acquired shell: ${shell != null}")
                if (cont.isActive) cont.resume(shell)
            }
        }

    private companion object {
        const val TAG = "EmulatorSetup"
        const val OPERATION_TIMEOUT_MS = 20_000L
    }
}

private fun PrivilegedShell.readText(path: String): String? {
    val proc = shell("cat '$path' 2>/dev/null") ?: return null
    return try {
        proc.inputStream.readBytes().decodeToString().also { proc.waitFor() }
            .also { Log.i("EmulatorSetup", "read $path -> ${it.length} chars") }
    } catch (e: Exception) {
        Log.w("EmulatorSetup", "read $path failed", e)
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
        Log.i("EmulatorSetup", "wrote $path (${content.length} chars)")
        true
    } catch (e: Exception) {
        Log.w("EmulatorSetup", "write $path failed", e)
        false
    } finally {
        proc.destroy()
    }
}
