package com.joegec.joycon2android.emulator

import android.content.pm.PackageManager
import android.util.Log
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.GetRemoteMotionUseCase
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.dsu.emulator.DolphinDsuConfig
import com.joegec.joycon2android.dsu.emulator.DolphinWiimoteConfig
import com.joegec.joycon2android.dsu.emulator.EdenDsuConfig
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.emulatorconfig.DolphinPaths
import com.joegec.joycon2android.emulatorconfig.EdenPaths
import com.joegec.joycon2android.gamepad.emulator.DolphinGcpadConfig
import com.joegec.joycon2android.gamepad.emulator.EdenGamepad
import com.joegec.joycon2android.gamepad.emulator.EdenGamepadConfig
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.output.EmulatorKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Best-effort: some OEM builds deny even the shell user another app's `Android/data`. */
class EmulatorSetup(
    private val packageManager: PackageManager,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
    private val scope: CoroutineScope,
    private val gamepadDevices: () -> Map<Int, EdenGamepad>,
    private val gamepadControllerNumbers: () -> Map<Int, Int>,
    private val getControllerMapping: GetEffectiveControllerMappingUseCase,
    private val getRemoteMotion: GetRemoteMotionUseCase,
    private val markSingleJoycons: (List<PlayerState>) -> List<PlayerState>,
) {

    // Read up front: the generators are synchronous.
    private suspend fun mappingLookup(
        console: Console,
        players: List<PlayerState>,
    ): (PlayerBody) -> Map<String, String> {
        val stored = bodiesOf(players).associateWith { getControllerMapping(console, it) }
        return { body -> stored[body] ?: MappingPresets.default(console).entries(body.side) }
    }

    private suspend fun motionLookup(
        console: Console,
        players: List<PlayerState>,
    ): (PlayerBody) -> RemoteMotion {
        val stored = bodiesOf(players).associateWith { getRemoteMotion(console, it) }
        return { body -> stored[body] ?: MappingPresets.default(console).motion }
    }

    private fun bodiesOf(players: List<PlayerState>) = players.mapNotNull { it.body() }.distinct()

    fun targets(): List<EmulatorTarget> = buildList {
        if (isInstalled(DolphinPaths.PACKAGE)) {
            add(EmulatorTarget(DOLPHIN_WII, DolphinPaths.PACKAGE, "Dolphin (Wii)", EmulatorKind.DOLPHIN_WII))
            add(EmulatorTarget(DOLPHIN_GAMECUBE, DolphinPaths.PACKAGE, "Dolphin (GameCube)", EmulatorKind.DOLPHIN_GAMECUBE))
        }
        if (isInstalled(EdenPaths.PACKAGE)) {
            add(EmulatorTarget(EdenPaths.PACKAGE, EdenPaths.PACKAGE, "Eden", EmulatorKind.EDEN))
        }
        if (isInstalled(EdenPaths.NIGHTLY_PACKAGE)) {
            add(EmulatorTarget(EdenPaths.NIGHTLY_PACKAGE, EdenPaths.NIGHTLY_PACKAGE, "Eden Nightly", EmulatorKind.EDEN))
        }
        addAll(EmulatorTarget.OTHERS)
    }

    /** Writes [target]'s config for the device it reads: [OutputPlan.setUpThrough]. */
    suspend fun configure(
        target: EmulatorTarget,
        devices: Set<OutputDevice>,
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult {
        val packageName = target.packageName ?: return EmulatorSetupResult.FAILED
        return when (OutputPlan.setUpThrough(target.kind, devices)) {
            OutputDevice.PAD -> configureGamepad(packageName, players, closeEmulator)
            OutputDevice.MOTION -> configureDsu(packageName, players, closeEmulator)
            OutputDevice.KEYBOARD, OutputDevice.MOUSE, null -> EmulatorSetupResult.FAILED
        }
    }

    private fun isInstalled(pkg: String) =
        runCatching { packageManager.getPackageInfo(pkg, 0) }.isSuccess

    suspend fun configureDsu(
        emulatorId: String,
        players: List<PlayerState>,
        closeEmulator: Boolean = false,
    ): EmulatorSetupResult = if (emulatorId in EdenPaths.PACKAGES) {
        configureEdenDsu(emulatorId, players, closeEmulator)
    } else {
        configureDolphinDsu(players, closeEmulator)
    }

    private suspend fun configureEdenDsu(
        emulatorId: String,
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult =
        bounded("eden-dsu") {
            val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
            shell.settle(emulatorId, closeEmulator)?.let { return@bounded it }
            val path = EdenDsuConfig.path(emulatorId)
            val written = shell.writeText(
                path,
                EdenDsuConfig.merge(
                    shell.readText(path),
                    markSingleJoycons(players),
                    mappingLookup(Console.SWITCH_PRO, players),
                ),
            )
            if (written) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
        }

    private suspend fun configureDolphinDsu(
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult = bounded("dsu") {
        val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
        shell.settle(DolphinPaths.PACKAGE, closeEmulator)?.let { return@bounded it }

        val dsuMerged = DolphinDsuConfig.merge(shell.readText(DolphinDsuConfig.path))
        shell.writeText(DolphinDsuConfig.path, dsuMerged)
        val dsuOk = shell.readText(DolphinDsuConfig.path)?.contains("127.0.0.1:${DsuConfig.PORT}") == true

        val mapping = mappingLookup(Console.WIIMOTE_NUNCHUK, players)
        val configurable = players.any { DolphinWiimoteConfig.writesFor(it, mapping) }
        val wiimoteOk = !configurable || shell.writeText(
            DolphinWiimoteConfig.path,
            DolphinWiimoteConfig.merge(
                shell.readText(DolphinWiimoteConfig.path),
                players,
                motionLookup(Console.WIIMOTE_NUNCHUK, players),
                mapping,
            ),
        )

        if (dsuOk && wiimoteOk) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
    }

    suspend fun configureGamepad(
        emulatorId: String,
        players: List<PlayerState>,
        closeEmulator: Boolean = false,
    ): EmulatorSetupResult =
        bounded("gamepad") {
            val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
            val target = if (emulatorId in EdenPaths.PACKAGES) emulatorId else DolphinPaths.PACKAGE
            shell.settle(target, closeEmulator)?.let { return@bounded it }
            val written = if (emulatorId in EdenPaths.PACKAGES) {
                val path = EdenPaths.config(emulatorId)
                shell.writeText(
                    path,
                    EdenGamepadConfig.merge(
                        shell.readText(path),
                        markSingleJoycons(players),
                        gamepadDevices(),
                        mappingLookup(Console.SWITCH_PRO, players),
                    ),
                )
            } else {
                val mappings = DolphinGcpadConfig.merge(
                    shell.readText(DolphinGcpadConfig.path),
                    players,
                    gamepadControllerNumbers(),
                    mappingLookup(Console.GAMECUBE, players),
                )
                val mappingsOk = shell.writeText(DolphinGcpadConfig.path, mappings)
                // Dolphin's GameCube ports default to None.
                val core = DolphinGcpadConfig.mergeCore(shell.readText(DolphinGcpadConfig.corePath), players)
                val coreOk = shell.writeText(DolphinGcpadConfig.corePath, core)
                mappingsOk && coreOk
            }
            if (written) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
        }

    // Shell calls block in native code that cancellation can't interrupt, so they run on an outer
    // scope that is abandoned on timeout; otherwise a hung call pins the spinner.
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

        // Dolphin is one package with two controller setups, so each needs an id of its own.
        const val DOLPHIN_WII = "dolphin-wii"
        const val DOLPHIN_GAMECUBE = "dolphin-gamecube"
    }
}

/**
 * Null once the way is clear, else [EmulatorSetupResult.EMULATOR_RUNNING]. `pidof` can't tell a
 * cached process from a live one, so any process at all asks first.
 */
private fun PrivilegedShell.settle(packageName: String, closeIt: Boolean): EmulatorSetupResult? {
    if (!hasProcess(packageName)) return null
    if (!closeIt) return EmulatorSetupResult.EMULATOR_RUNNING
    forceStop(packageName)
    return null
}

// pidof needs the shell uid — an app can only ever see its own process.
private fun PrivilegedShell.hasProcess(packageName: String): Boolean {
    val proc = shell("pidof '$packageName'") ?: return false
    return try {
        proc.inputStream.readBytes().decodeToString().also { proc.waitFor() }.isNotBlank()
    } catch (e: Exception) {
        Log.w("EmulatorSetup", "pidof $packageName failed", e)
        false
    } finally {
        proc.destroy()
    }
}

private fun PrivilegedShell.forceStop(packageName: String) {
    val proc = shell("am force-stop '$packageName'") ?: return
    try {
        proc.waitFor()
        Log.i("EmulatorSetup", "force-stopped $packageName")
    } finally {
        proc.destroy()
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
