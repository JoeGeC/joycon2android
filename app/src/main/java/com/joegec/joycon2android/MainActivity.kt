package com.joegec.joycon2android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.ui.Joycon2ViewModel
import com.joegec.joycon2android.ui.JoyconScreen
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Joycon2AndroidTheme

class MainActivity : ComponentActivity() {

    private val viewModel: Joycon2ViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.recheckPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val permissionHandler = viewModel.permissionHandler

        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.all { it }) {
                viewModel.onPermissionsGranted()
                viewModel.startScan()
            } else {
                viewModel.onPermissionsDenied()
            }
        }

        setContent {
            Joycon2AndroidTheme {
                Surface(Modifier.fillMaxSize(), color = Background) {
                    val state by viewModel.uiState.collectAsState()
                    val gamepadEnabled by viewModel.gamepadEnabled.collectAsState()
                    val gamepadError by viewModel.gamepadError.collectAsState()
                    val permissionDenied by viewModel.permissionDenied.collectAsState()
                    JoyconScreen(
                        state = state,
                        gamepadEnabled = gamepadEnabled,
                        gamepadError = gamepadError,
                        permissionDenied = permissionDenied,
                        onScan = { permLauncher.launch(permissionHandler.requiredPermissions) },
                        onDisconnectAll = viewModel::disconnectAll,
                        onAssign = viewModel::assignToPlayer,
                        onUnassign = viewModel::unassign,
                        onDisconnect = viewModel::disconnect,
                        onGamepadToggle = { enabled ->
                            if (enabled) viewModel.enableGamepad()
                            else viewModel.disableGamepad()
                        },
                        onOpenSettings = { startActivity(permissionHandler.buildSettingsIntent()) },
                    )
                }
            }
        }
    }

}
