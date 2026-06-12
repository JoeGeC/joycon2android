package com.joegec.joycon2android.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.ble.BlePermissionHandler
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.service.Joycon2Service
import com.joegec.joycon2android.uhid.AdbState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak") // Service ref is cleared in onCleared/onServiceDisconnected
class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    val permissionHandler = BlePermissionHandler(application)

    private var service: Joycon2Service? = null
    private var bound = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _gamepadEnabled = MutableStateFlow(false)
    val gamepadEnabled: StateFlow<Boolean> = _gamepadEnabled.asStateFlow()

    private val _gamepadError = MutableStateFlow<String?>(null)
    val gamepadError: StateFlow<String?> = _gamepadError.asStateFlow()

    private val _dsuEnabled = MutableStateFlow(false)
    val dsuEnabled: StateFlow<Boolean> = _dsuEnabled.asStateFlow()

    private val _dsuError = MutableStateFlow<String?>(null)
    val dsuError: StateFlow<String?> = _dsuError.asStateFlow()

    private val _dsuClientCount = MutableStateFlow(0)
    val dsuClientCount: StateFlow<Int> = _dsuClientCount.asStateFlow()

    private val _dsuLanEnabled = MutableStateFlow(false)
    val dsuLanEnabled: StateFlow<Boolean> = _dsuLanEnabled.asStateFlow()

    private val _adbState = MutableStateFlow(AdbState.DISCONNECTED)
    val adbState: StateFlow<AdbState> = _adbState.asStateFlow()

    private val _adbError = MutableStateFlow<String?>(null)
    val adbError: StateFlow<String?> = _adbError.asStateFlow()

    // Whether the in-app wireless-debugging path is needed (Shizuku absent)
    private val _adbSetupNeeded = MutableStateFlow(false)
    val adbSetupNeeded: StateFlow<Boolean> = _adbSetupNeeded.asStateFlow()

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    private val collectionJobs = mutableListOf<Job>()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val svc = (binder as Joycon2Service.LocalBinder).service
            service = svc
            bound = true
            collectServiceState(svc)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
            cancelCollection()
        }
    }

    init {
        if (permissionHandler.isGranted()) {
            startAndBind()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelCollection()
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
        }
    }

    fun onPermissionsGranted() {
        _permissionDenied.value = false
        if (!bound) startAndBind()
    }

    fun recheckPermissions() {
        if (_permissionDenied.value && permissionHandler.isGranted()) {
            onPermissionsGranted()
        }
    }

    fun startScan() {
        service?.startScan()
    }

    fun stopScan() {
        service?.stopScan()
    }

    fun disconnectAll() {
        service?.disconnectAll()
    }

    fun assignToPlayer(address: String, player: PlayerNumber) {
        service?.assignToPlayer(address, player)
    }

    fun unassign(address: String) {
        service?.unassign(address)
    }

    fun disconnect(address: String) {
        service?.disconnect(address)
    }

    fun onPermissionsDenied() {
        _permissionDenied.value = true
    }

    fun enableGamepad() {
        service?.enableGamepad()
    }

    fun disableGamepad() {
        service?.disableGamepad()
    }

    fun enableDsu() {
        service?.enableDsu()
    }

    fun disableDsu() {
        service?.disableDsu()
    }

    fun setDsuLanEnabled(enabled: Boolean) {
        service?.setDsuLanEnabled(enabled)
    }

    fun startAdbPairing() {
        service?.startAdbPairing()
    }

    /**
     * Stops the service entirely — disconnects all devices and removes the notification.
     * Called when the user explicitly wants to shut everything down.
     */
    fun stopService() {
        service?.disconnectAll()
        val app = getApplication<Application>()
        app.stopService(Intent(app, Joycon2Service::class.java))
    }

    private fun startAndBind() {
        val app = getApplication<Application>()
        val intent = Intent(app, Joycon2Service::class.java)
        ContextCompat.startForegroundService(app, intent)
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun collectServiceState(svc: Joycon2Service) {
        collectInto(svc.uiState, _uiState)
        collectInto(svc.gamepadEnabled, _gamepadEnabled)
        collectInto(svc.gamepadError, _gamepadError)
        collectInto(svc.dsuEnabled, _dsuEnabled)
        collectInto(svc.dsuError, _dsuError)
        collectInto(svc.dsuClientCount, _dsuClientCount)
        collectInto(svc.dsuLanEnabled, _dsuLanEnabled)
        collectInto(svc.adbState, _adbState)
        collectInto(svc.adbError, _adbError)
        _adbSetupNeeded.value = !svc.shizukuAvailable
    }

    private fun <T> collectInto(source: StateFlow<T>, target: MutableStateFlow<T>) {
        collectionJobs += viewModelScope.launch {
            source.collect { target.value = it }
        }
    }

    private fun cancelCollection() {
        collectionJobs.forEach { it.cancel() }
        collectionJobs.clear()
    }

}
