package com.joegec.joycon2android.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joegec.joycon2android.AppContainer
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingViewModel
import com.joegec.joycon2android.buttonmapping.presentation.GroupLayoutPickerDialog
import com.joegec.joycon2android.controllerMappingViewModel
import com.joegec.joycon2android.ui.theme.Joycon2AndroidTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Opened from the home-screen widget, over the home screen. */
class GroupLayoutPickerActivity : ComponentActivity() {

    private val container: AppContainer get() = (application as JoyconApplication).container

    private val mappingViewModel: ControllerMappingViewModel by viewModels {
        viewModelFactory { initializer { container.controllerMappingViewModel() } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bodies = container.observeSession()
            .map { session -> session.activePlayers.mapNotNull { it.body() } }
            .distinctUntilChanged()
        setContent {
            Joycon2AndroidTheme {
                val players by bodies.collectAsState(initial = null)
                val state by mappingViewModel.uiState.collectAsState()
                val warnUnsavedMapping by container.observeWarnUnsavedMapping().collectAsState(initial = true)

                LaunchedEffect(players) {
                    val current = players ?: return@LaunchedEffect
                    if (current.isEmpty()) finish() else mappingViewModel.edit(container.consoleInView(), current)
                }
                state?.let {
                    GroupLayoutPickerDialog(
                        state = it,
                        warnUnsavedMapping = warnUnsavedMapping,
                        onSelect = { layoutId -> mappingViewModel.selectGlobalLayout(layoutId)?.invokeOnCompletion { finish() } },
                        onStopWarningUnsavedMapping = { lifecycleScope.launch { container.setWarnUnsavedMapping(false) } },
                        onDismiss = ::finish,
                    )
                }
            }
        }
    }
}
