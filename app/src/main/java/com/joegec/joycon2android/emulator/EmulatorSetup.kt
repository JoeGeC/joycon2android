package com.joegec.joycon2android.emulator

import android.content.pm.PackageManager
import android.util.Log
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.GetRemoteMotionUseCase
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.rumblePercent
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
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.selected
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
        return { body -> stored[body] ?: MappingPresets.default(console).motion(body.side) }
    }

    private fun bodiesOf(players: List<PlayerState>) = players.mapNotNull { it.body() }.distinct()

    /** The installed emulators Set up writes for [playing]. */
    fun targets(playing: Playing): List<EmulatorTarget> = when (playing.emulator) {
        EmulatorKind.EDEN -> edenTargets()
        EmulatorKind.DOLPHIN_GAMECUBE -> listOfNotNull(dolphin(EmulatorKind.DOLPHIN_GAMECUBE))
        EmulatorKind.DOLPHIN_WII -> listOfNotNull(dolphin(EmulatorKind.DOLPHIN_WII))
        EmulatorKind.OTHER, null -> emptyList()
    }

    /** What a per-device tab had picked, as it listed them, for [com.joegec.joycon2android.output.PlayingMigration]. */
    fun tabTarget(mode: OutputMode, id: String?): EmulatorTarget? {
        val listed = when (mode) {
            OutputMode.GAMEPAD -> listOfNotNull(dolphin(EmulatorKind.DOLPHIN_GAMECUBE)) + edenTargets() + EmulatorTarget.OTHER
            OutputMode.MOTION -> listOfNotNull(dolphin(EmulatorKind.DOLPHIN_WII)) + edenTargets() + EmulatorTarget.OTHER
            OutputMode.MOUSE_KEYBOARD -> emptyList()
        }
        return listed.selected(id)
    }

    private fun edenTargets() = listOfNotNull(
        EmulatorTarget(EdenPaths.PACKAGE, EdenPaths.PACKAGE, "Eden", EmulatorKind.EDEN).takeIf { isInstalled(EdenPaths.PACKAGE) },
        EmulatorTarget(EdenPaths.NIGHTLY_PACKAGE, EdenPaths.NIGHTLY_PACKAGE, "Eden Nightly", EmulatorKind.EDEN)
            .takeIf { isInstalled(EdenPaths.NIGHTLY_PACKAGE) },
    )

    private fun dolphin(kind: EmulatorKind): EmulatorTarget? {
        if (!isInstalled(DolphinPaths.PACKAGE)) return null
        return if (kind == EmulatorKind.DOLPHIN_WII) EmulatorTarget(DOLPHIN_WII, DolphinPaths.PACKAGE, "Dolphin (Wii)", kind)
        else EmulatorTarget(DOLPHIN_GAMECUBE, DolphinPaths.PACKAGE, "Dolphin (GameCube)", kind)
    }

    /** Writes [target]'s config for [playing]; [motion] is Eden's, which binds it over DSU or not at all. */
    suspend fun configure(
        target: EmulatorTarget,
        playing: Playing,
        motion: Boolean,
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult {
        val packageName = target.packageName ?: return EmulatorSetupResult.FAILED
        return when (playing) {
            Playing.SWITCH -> configureEden(packageName, motion, players, closeEmulator)
            Playing.GAMECUBE -> configureGamecube(players, closeEmulator)
            Playing.WII -> configureWii(players, closeEmulator)
            Playing.OTHER_EMULATOR, Playing.ANDROID, Playing.PC -> EmulatorSetupResult.FAILED
        }
    }

    private fun isInstalled(pkg: String) =
        runCatching { packageManager.getPackageInfo(pkg, 0) }.isSuccess

    // Buttons go to each player's pad, since Eden rumbles only the device they're on; a player without
    // one keeps them on DSU.
    private suspend fun configureEden(
        packageName: String,
        motion: Boolean,
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult =
        bounded("eden") {
            val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
            shell.settle(packageName, closeEmulator)?.let { return@bounded it }
            val path = EdenPaths.config(packageName)
            val marked = markSingleJoycons(players)
            val mapping = mappingLookup(Console.SWITCH_PRO, players)
            val existing = shell.readText(path)
            val config = if (motion) {
                EdenGamepadConfig.bindToPads(EdenDsuConfig.merge(existing, marked, mapping), marked, gamepadDevices(), mapping)
            } else {
                EdenGamepadConfig.merge(EdenDsuConfig.withoutMotion(existing), marked, gamepadDevices(), mapping)
            }
            if (shell.writeText(path, config)) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
        }

    private suspend fun configureWii(
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult = bounded("wii") {
        val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
        shell.settle(DolphinPaths.PACKAGE, closeEmulator)?.let { return@bounded it }

        val dsuMerged = DolphinDsuConfig.merge(shell.readText(DolphinDsuConfig.path))
        shell.writeText(DolphinDsuConfig.path, dsuMerged)
        val dsuOk = shell.readText(DolphinDsuConfig.path)?.contains("127.0.0.1:${DsuConfig.PORT}") == true

        val motion = motionLookup(Console.WIIMOTE_NUNCHUK, players)
        val mapping = mappingLookup(Console.WIIMOTE_NUNCHUK, players)
        val configurable = players.any { DolphinWiimoteConfig.writesFor(it, motion) }
        val padIds = gamepadControllerNumbers()
        val remotes = DolphinWiimoteConfig.merge(shell.readText(DolphinWiimoteConfig.path), players, motion, mapping)
        val wiimoteOk = !configurable || shell.writeText(
            DolphinWiimoteConfig.path,
            DolphinWiimoteConfig.withRumble(remotes, players, motion) { body ->
                padIds[body.player.index]?.takeIf { mapping(body).rumblePercent > 0 }
            },
        )

        if (dsuOk && wiimoteOk) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
    }

    private suspend fun configureGamecube(
        players: List<PlayerState>,
        closeEmulator: Boolean,
    ): EmulatorSetupResult =
        bounded("gamecube") {
            val shell = awaitShell() ?: return@bounded EmulatorSetupResult.NO_PRIVILEGED_ACCESS
            shell.settle(DolphinPaths.PACKAGE, closeEmulator)?.let { return@bounded it }
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
            if (mappingsOk && coreOk) EmulatorSetupResult.SUCCESS else EmulatorSetupResult.FAILED
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
