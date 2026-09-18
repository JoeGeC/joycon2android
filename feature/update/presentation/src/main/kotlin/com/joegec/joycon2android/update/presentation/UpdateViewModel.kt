package com.joegec.joycon2android.update.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.update.AvailableUpdate
import com.joegec.joycon2android.update.CheckForUpdateUseCase
import com.joegec.joycon2android.update.InstallProgress
import com.joegec.joycon2android.update.InstallUpdateUseCase
import com.joegec.joycon2android.update.SkipUpdateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Checks once per launch; a failed check leaves the prompt closed rather than reporting anything. */
class UpdateViewModel(
    private val checkForUpdate: CheckForUpdateUseCase,
    private val skipUpdate: SkipUpdateUseCase,
    private val installUpdate: InstallUpdateUseCase,
) : ViewModel() {

    private val _availableUpdate = MutableStateFlow<AvailableUpdate?>(null)
    val availableUpdate: StateFlow<AvailableUpdate?> = _availableUpdate.asStateFlow()

    private val _installProgress = MutableStateFlow<InstallProgress?>(null)
    val installProgress: StateFlow<InstallProgress?> = _installProgress.asStateFlow()

    init {
        viewModelScope.launch { _availableUpdate.value = checkForUpdate() }
    }

    fun install() {
        val update = _availableUpdate.value ?: return
        if (_installProgress.value is InstallProgress.Downloading) return
        viewModelScope.launch {
            installUpdate(update).collect { _installProgress.value = it }
        }
    }

    fun skip() {
        val update = _availableUpdate.value ?: return
        viewModelScope.launch { skipUpdate(update) }
        dismiss()
    }

    fun dismiss() {
        _availableUpdate.value = null
        _installProgress.value = null
    }
}
