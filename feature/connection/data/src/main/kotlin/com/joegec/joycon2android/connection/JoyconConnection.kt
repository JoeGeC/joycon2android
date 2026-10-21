package com.joegec.joycon2android.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joegec.joycon2android.connection.console.ConsoleChannel
import com.joegec.joycon2android.connection.console.ConsoleSession
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class JoyconConnection(
    private val context: Context,
    val detectedModel: ControllerModel,
    typeOverride: ControllerModel?,
    val deviceName: String,
    private val onReady: () -> Unit,
    private val onDisconnected: (() -> Unit)? = null,
) {
    companion object {
        private const val TAG = "Joycon2"
        private const val DESIRED_MTU = 247

        // A controller that speaks the common protocol answers the SPI read or starts streaming
        // well within this window; one that answers later is moved back by its first report.
        private const val CONSOLE_FALLBACK_MS = 1_500L

        private const val RETURN_WINDOW_MS = 120_000L
    }

    @Volatile var typeOverride: ControllerModel? = typeOverride
        private set
    val model: ControllerModel get() = typeOverride ?: detectedModel
    @Volatile var side: Side = model.defaultSide
        private set

    private val _connectionState = MutableStateFlow(
        JoyconConnectionState(connecting = true, deviceName = deviceName)
    )
    val connectionState: StateFlow<JoyconConnectionState> = _connectionState.asStateFlow()

    private val battery = SteadyBattery()
    private val _input = MutableStateFlow(JoyconInput())
    val input: StateFlow<JoyconInput> = _input.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val ops = GattOpQueue()
    private val returnWindow = ReturnWindow(RETURN_WINDOW_MS, MainThreadScheduler(), ::giveUpOnReturn)
    @Volatile private var calibrator = InputCalibrator(model)
    @Volatile private var factory = FactoryCalibration()
    private var gatt: BluetoothGatt? = null
    private var device: BluetoothDevice? = null
    private var common: CommonSession? = null
    @Volatile private var console: ConsoleSession? = null

    // The console protocol takes over for as long as it runs; everything else goes through here.
    private val protocol: ControllerProtocol? get() = console ?: common

    @Volatile var initComplete = false
        private set
    @Volatile private var highPriority = false
    @Volatile private var securityRequested = false
    @Volatile private var servicesDiscovered = false
    @Volatile private var commonReportSeen = false
    @Volatile private var commandReplySeen = false
    @Volatile private var assignedPlayer: PlayerNumber? = null
    @Volatile private var everReady = false
    @Volatile private var released = false

    val awaitingReturn: Boolean get() = returnWindow.isOpen

    private val mayUseConsole: Boolean get() = ConsoleFallback.allows(model)

    fun connect(device: BluetoothDevice) {
        this.device = device
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    /** docs/protocol.md#android-workarounds */
    fun onSecurityRequested() {
        if (securityRequested) return
        securityRequested = true
        Log.i(TAG, "[$side] SMP security request received")
        val g = gatt ?: return
        if (servicesDiscovered && !commonChannelAnswered()) mainHandler.post { startConsoleSession(g) }
    }

    fun onPairingFailed() {
        protocol?.onPairingFailed()
    }

    fun reassertPriority() {
        if (initComplete) protocol?.setHighPriority(highPriority)
    }

    fun setHighPriority(enabled: Boolean) {
        highPriority = enabled
        if (initComplete) protocol?.setHighPriority(enabled)
    }

    // A new side reads the sticks from other offsets, so their learned centres no longer apply.
    fun overrideType(override: ControllerModel?) {
        typeOverride = override
        side = model.defaultSide
        calibrator = InputCalibrator(model).apply { useFactory(factory) }
        if (initComplete) common?.read(FactoryCalibrationReads.commands(model))
    }

    fun setPlayerLed(player: PlayerNumber) {
        assignedPlayer = player
        protocol?.setPlayerLed(player.ledBitmask)
    }

    fun clearPlayerLed() {
        assignedPlayer = null
        protocol?.setPlayerLed(ControllerCommands.LED_ALL_ON)
    }

    /** A scan found this controller while it was awaited, so connect now rather than on its own time. */
    fun reconnect(device: BluetoothDevice) {
        if (!awaitingReturn) return
        Log.i(TAG, "[$side] Found by a scan while awaited; connecting directly")
        gatt?.close()
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        released = true
        returnWindow.close()
        stopProtocols()
        mainHandler.removeCallbacksAndMessages(null)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        ops.reset()
        _connectionState.value = JoyconConnectionState()
        _input.value = JoyconInput()
        battery.reset()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "[$side] Connected. Requesting MTU $DESIRED_MTU")
                    returnWindow.close()
                    _connectionState.value = JoyconConnectionState(
                        connected = true, deviceName = deviceName
                    )
                    g.requestMtu(DESIRED_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "[$side] Disconnected (status=$status)")
                    endSession(g)
                    _input.value = JoyconInput()
                    battery.reset()
                    if (!released && (everReady || awaitingReturn) && awaitReturn(status)) return
                    _connectionState.value = JoyconConnectionState(
                        deviceName = deviceName,
                        error = if (status != BluetoothGatt.GATT_SUCCESS) {
                            "Connection lost (status $status)"
                        } else null
                    )
                    onDisconnected?.invoke()
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "[$side] MTU=$mtu. Discovering services.")
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            Log.i(TAG, "[$side] Services discovered (status=$status)")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                fail("Service discovery failed")
                return
            }

            servicesDiscovered = true
            if (securityRequested && mayUseConsole) {
                startConsoleSession(g)
                return
            }
            if (!CommonChannel.hasService(g)) {
                fail("Not a compatible Joy-Con 2")
                return
            }
            val channel = CommonChannel.find(g)
            if (channel == null) {
                fail("Missing BLE characteristics")
                return
            }
            common = CommonSession(
                gatt = g,
                channel = channel,
                side = { side },
                ops = ops,
                calibrationReads = FactoryCalibrationReads.commands(model),
                initialLedBitmask = assignedPlayer?.ledBitmask ?: ControllerCommands.LED_ALL_ON,
                onInput = ::publishInput,
                onAccentColor = ::publishAccentColor,
                onCommandReply = ::applyFactoryCalibration,
                onReady = {
                    onProtocolReady()
                    mainHandler.postDelayed({ fallBackToConsoleIfSilent(g) }, CONSOLE_FALLBACK_MS)
                },
            ).also { it.start() }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.i(TAG, "[$side] CCCD write status=$status")
            ops.complete(status)
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
            Log.d(TAG, "[$side] Char write status=$status initComplete=$initComplete")
            ops.complete(status)
        }

        @Deprecated("Deprecated in Java - used for API < 33")
        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic
        ) {
            handleCharacteristicChanged(ch.uuid, ch.value ?: return)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray
        ) {
            handleCharacteristicChanged(ch.uuid, value)
        }
    }

    private fun handleCharacteristicChanged(uuid: UUID, data: ByteArray) {
        if (uuid == CommonChannel.INPUT) {
            commonReportSeen = true
            // Only a controller that speaks the common protocol notifies here, so the fallback misfired.
            if (console != null && common != null) returnToCommonChannel()
        } else if (console == null && uuid == CommonChannel.COMMAND_RESPONSE) {
            commandReplySeen = true
        }
        protocol?.onCharacteristicChanged(uuid, data)
    }

    private fun commonChannelAnswered() = commonReportSeen || commandReplySeen

    private fun fallBackToConsoleIfSilent(g: BluetoothGatt) {
        if (commonChannelAnswered() || !mayUseConsole) return
        Log.i(TAG, "[$side] No reply on the common channel; switching to the console protocol")
        startConsoleSession(g)
    }

    @Synchronized
    private fun startConsoleSession(g: BluetoothGatt) {
        if (!mayUseConsole || console != null || commonReportSeen || gatt !== g) return
        val channel = ConsoleChannel.find(g)
        if (channel == null) {
            fail("Not a compatible Joy-Con 2")
            return
        }
        ops.clear()
        initComplete = false
        _connectionState.value = _connectionState.value.copy(ready = false)
        console = ConsoleSession(
            gatt = g,
            channel = channel,
            ops = ops,
            initialLedBitmask = assignedPlayer?.ledBitmask ?: ControllerCommands.LED_ALL_ON,
            onInput = ::publishInput,
            onAccentColor = ::publishAccentColor,
            onReady = ::onProtocolReady,
        ).also { it.start() }
    }

    private fun returnToCommonChannel() {
        Log.i(TAG, "[$side] Common-channel report arrived; leaving the console protocol")
        stopConsoleSession()
        common?.resume()
        initComplete = true
        _connectionState.value = _connectionState.value.copy(
            connected = true, ready = true, deviceName = deviceName
        )
        protocol?.setHighPriority(highPriority)
    }

    private fun onProtocolReady() {
        initComplete = true
        everReady = true
        _connectionState.value = _connectionState.value.copy(
            connected = true, ready = true, deviceName = deviceName
        )
        if (highPriority) protocol?.setHighPriority(true)
        onReady()
    }

    // Everything that belongs to one link, so the next one starts its own detection over.
    private fun endSession(g: BluetoothGatt) {
        stopProtocols()
        ops.reset()
        g.close()
        gatt = null
        initComplete = false
        securityRequested = false
        servicesDiscovered = false
        commonReportSeen = false
        commandReplySeen = false
    }

    // Android keeps a pending connect armed after a drop: docs/protocol.md#coming-back-after-a-drop
    private fun awaitReturn(status: Int): Boolean {
        val target = device ?: return false
        val reopened = target.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
            ?: return false
        Log.i(TAG, "[$side] Connection lost (status $status); waiting for the controller to come back")
        gatt = reopened
        returnWindow.open()
        _connectionState.value = JoyconConnectionState(deviceName = deviceName, connectionLost = true)
        return true
    }

    private fun giveUpOnReturn() {
        Log.i(TAG, "[$side] Gave up waiting for the controller")
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionState.value = JoyconConnectionState(deviceName = deviceName)
        onDisconnected?.invoke()
    }

    private fun stopConsoleSession() {
        console?.stop()
        console = null
    }

    private fun stopProtocols() {
        stopConsoleSession()
        common?.stop()
        common = null
    }

    private fun publishInput(input: JoyconInput) {
        _input.value = calibrator.calibrate(input).copy(battery = battery.steady(input.battery))
    }

    private fun applyFactoryCalibration(reply: ByteArray) {
        FactoryCalibrationReads.update(factory, reply)?.let {
            factory = it
            calibrator.useFactory(it)
        }
    }

    private fun publishAccentColor(color: Int) {
        _connectionState.value = _connectionState.value.copy(accentColor = color)
    }

    private fun fail(message: String) {
        _connectionState.value = JoyconConnectionState(error = message, deviceName = deviceName)
    }
}
