package com.joegec.joycon2android.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.AppContainer
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.ble.BlePermissionHandler
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.power.BatteryOptimizationExemption
import com.joegec.joycon2android.service.Joycon2Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    val permissionHandler = BlePermissionHandler(application)
    val batteryOptimizationExemption = BatteryOptimizationExemption(application)

    private var bound = false

    // Changes with every packet, so the UI reads it split: docs/architecture.md#live-input
    private val _uiState = MutableStateFlow(AppUiState())

    val session: StateFlow<AppUiState> = _uiState
        .map(AppUiState::withoutLiveInput)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppUiState())

    val heldControllers: StateFlow<List<PlayerState>> = session
        .map { it.activePlayers }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val liveInputs: StateFlow<Map<String, JoyconInput>> = _uiState
        .map { it.liveInputs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    private val container: AppContainer
        get() = (getApplication<Application>() as JoyconApplication).container

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    private val _batteryRestricted = MutableStateFlow(!batteryOptimizationExemption.isGranted())
    val batteryRestricted: StateFlow<Boolean> = _batteryRestricted.asStateFlow()

    val viewMode: StateFlow<ConnectionViewMode> = container.observeViewMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectionViewMode.DETAILED)

    // Bound only for the service's lifetime; state comes from the container, not the binder.
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    init {
        if (permissionHandler.isGranted()) {
            startAndBind()
        }
        viewModelScope.launch {
            container.observeSession().collect { _uiState.value = it }
        }
    }

    override fun onCleared() {
        super.onCleared()
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

    fun recheckBatteryOptimization() {
        _batteryRestricted.value = !batteryOptimizationExemption.isGranted()
    }

    fun startScan() = container.startScan()

    fun stopScan() = container.stopScan()

    fun disconnectAll() = container.disconnectAll()

    fun assignToPlayer(address: String, player: PlayerNumber) = container.assignController(address, player)

    fun unassign(address: String) = container.unassignController(address)

    fun disconnect(address: String) = container.disconnectController(address)

    fun setControllerType(address: String, model: ControllerModel?) = container.setControllerType(address, model)

    fun onPermissionsDenied() {
        _permissionDenied.value = true
    }

    fun setViewMode(mode: ConnectionViewMode) {
        viewModelScope.launch { container.setViewMode(mode) }
    }

    fun stopService() {
        container.disconnectAll()
        val app = getApplication<Application>()
        app.stopService(Intent(app, Joycon2Service::class.java))
    }

    private fun startAndBind() {
        val app = getApplication<Application>()
        val intent = Intent(app, Joycon2Service::class.java)
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}
