package com.joegec.joycon2android

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
                        onScan = { permLauncher.launch(requiredPermissions()) },
                        onDisconnectAll = viewModel::disconnectAll,
                        onAssign = viewModel::assignToPlayer,
                        onUnassign = viewModel::unassign,
                        onDisconnect = viewModel::disconnect,
                        onGamepadToggle = { enabled ->
                            if (enabled) viewModel.enableGamepad()
                            else viewModel.disableGamepad()
                        },
                        onOpenSettings = ::openAppSettings,
                    )
                }
            }
        }
    }

    private fun openAppSettings() {
        val nearbyIntent = Intent("android.settings.MANAGE_APP_PERMISSION").apply {
            putExtra("android.intent.extra.PACKAGE_NAME", packageName)
            putExtra("android.intent.extra.PERMISSION_GROUP_NAME",
                "android.permission-group.NEARBY_DEVICES")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            nearbyIntent.resolveActivity(packageManager) != null
        ) {
            startActivity(nearbyIntent)
        } else {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(fallback)
        }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
}
