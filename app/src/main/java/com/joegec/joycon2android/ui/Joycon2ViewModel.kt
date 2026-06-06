package com.joegec.joycon2android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.JoyconInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    private val manager = Joycon2Manager(application)

    private val _state = MutableStateFlow(ControllerState())
    val state: StateFlow<ControllerState> = _state.asStateFlow()

    init {
        // Collect the manager's own state (scanning/error/connection events)
        viewModelScope.launch {
            manager.state.collectLatest { managerState ->
                _state.value = managerState
                // When connections appear, start collecting their individual flows
                startCollectingConnections()
            }
        }
    }

    private var leftCollecting = false
    private var rightCollecting = false

    private fun startCollectingConnections() {
        val left = manager.getLeftConnection()
        val right = manager.getRightConnection()

        if (left != null && !leftCollecting) {
            leftCollecting = true
            viewModelScope.launch {
                left.connectionState.collectLatest { rebuildState() }
            }
            viewModelScope.launch {
                left.input.collectLatest { rebuildState() }
            }
        }

        if (right != null && !rightCollecting) {
            rightCollecting = true
            viewModelScope.launch {
                right.connectionState.collectLatest { rebuildState() }
            }
            viewModelScope.launch {
                right.input.collectLatest { rebuildState() }
            }
        }
    }

    private fun rebuildState() {
        val left = manager.getLeftConnection()
        val right = manager.getRightConnection()
        _state.value = ControllerState(
            scanning = manager.state.value.scanning,
            error = manager.state.value.error,
            left = left?.connectionState?.value ?: JoyconConnectionState(),
            right = right?.connectionState?.value ?: JoyconConnectionState(),
            leftInput = left?.input?.value ?: JoyconInput(),
            rightInput = right?.input?.value ?: JoyconInput(),
        )
    }

    fun startScan() {
        manager.startScan()
    }

    fun stop() {
        leftCollecting = false
        rightCollecting = false
        manager.stop()
    }

    fun onPermissionsDenied() {
        manager.emitError(getApplication<Application>().getString(R.string.error_permissions_denied))
    }

    override fun onCleared() {
        super.onCleared()
        manager.stop()
    }
}
