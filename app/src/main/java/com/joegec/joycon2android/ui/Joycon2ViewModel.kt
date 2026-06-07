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
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.service.Joycon2Service
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak") // Service ref is cleared in onCleared/onServiceDisconnected
class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    private var service: Joycon2Service? = null
    private var bound = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _gamepadEnabled = MutableStateFlow(false)
    val gamepadEnabled: StateFlow<Boolean> = _gamepadEnabled.asStateFlow()

    private val _gamepadError = MutableStateFlow<String?>(null)
    val gamepadError: StateFlow<String?> = _gamepadError.asStateFlow()

    private var stateJob: Job? = null
    private var gamepadEnabledJob: Job? = null
    private var gamepadErrorJob: Job? = null

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
        startAndBind()
    }

    override fun onCleared() {
        super.onCleared()
        cancelCollection()
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
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
        service?.emitError(getApplication<Application>().getString(R.string.error_permissions_denied))
    }

    fun enableGamepad() {
        service?.enableGamepad()
    }

    fun disableGamepad() {
        service?.disableGamepad()
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
        stateJob = viewModelScope.launch {
            svc.uiState.collect { _uiState.value = it }
        }
        gamepadEnabledJob = viewModelScope.launch {
            svc.gamepadEnabled.collect { _gamepadEnabled.value = it }
        }
        gamepadErrorJob = viewModelScope.launch {
            svc.gamepadError.collect { _gamepadError.value = it }
        }
    }

    private fun cancelCollection() {
        stateJob?.cancel()
        gamepadEnabledJob?.cancel()
        gamepadErrorJob?.cancel()
    }
}
