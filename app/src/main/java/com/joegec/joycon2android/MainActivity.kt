package com.joegec.joycon2android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.joegec.joycon2android.ui.Joycon2ViewModel
import com.joegec.joycon2android.ui.JoyconScreen
import com.joegec.joycon2android.ui.components.AdbSetupState
import com.joegec.joycon2android.ui.components.DsuCardState
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Joycon2AndroidTheme

class MainActivity : ComponentActivity() {

    private val viewModel: Joycon2ViewModel by viewModels()
    private val notificationsGranted = mutableStateOf(true)
    private var notificationAsked = false

    override fun onResume() {
        super.onResume()
        viewModel.recheckPermissions()
        notificationsGranted.value = hasNotificationPermission()
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

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

        // The wireless-debugging path prompts for the pairing code via a notification
        val notificationPermLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted -> notificationsGranted.value = granted }

        notificationsGranted.value = hasNotificationPermission()

        // Only requested when the user opts into the wireless-debugging path, never on launch
        val enableNotifications = {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (notificationAsked && !shouldShowRequestPermissionRationale(perm)) {
                    startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
                    )
                } else {
                    notificationAsked = true
                    notificationPermLauncher.launch(perm)
                }
            }
        }

        setContent {
            Joycon2AndroidTheme {
                Surface(Modifier.fillMaxSize(), color = Background) {
                    val state by viewModel.uiState.collectAsState()
                    val gamepadEnabled by viewModel.gamepadEnabled.collectAsState()
                    val gamepadError by viewModel.gamepadError.collectAsState()
                    val dsuEnabled by viewModel.dsuEnabled.collectAsState()
                    val dsuError by viewModel.dsuError.collectAsState()
                    val dsuClientCount by viewModel.dsuClientCount.collectAsState()
                    val dsuLanEnabled by viewModel.dsuLanEnabled.collectAsState()
                    val adbState by viewModel.adbState.collectAsState()
                    val adbError by viewModel.adbError.collectAsState()
                    val adbSetupNeeded by viewModel.adbSetupNeeded.collectAsState()
                    val permissionDenied by viewModel.permissionDenied.collectAsState()
                    JoyconScreen(
                        state = state,
                        gamepadEnabled = gamepadEnabled,
                        gamepadError = gamepadError,
                        dsuState = DsuCardState(
                            enabled = dsuEnabled,
                            error = dsuError,
                            clientCount = dsuClientCount,
                            lanEnabled = dsuLanEnabled,
                            showSlotLimitNote = state.activePlayers.any { it.player.index > 4 },
                        ),
                        adbSetup = AdbSetupState(
                            needed = adbSetupNeeded,
                            state = adbState,
                            error = adbError,
                            notificationsGranted = notificationsGranted.value,
                        ),
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
                        onDsuToggle = { enabled ->
                            if (enabled) viewModel.enableDsu()
                            else viewModel.disableDsu()
                        },
                        onDsuLanToggle = viewModel::setDsuLanEnabled,
                        onAdbDisconnect = viewModel::disconnectAdb,
                        onEnableNotifications = enableNotifications,
                        onStartAdbPairing = viewModel::startAdbPairing,
                        onOpenSettings = { startActivity(permissionHandler.buildSettingsIntent()) },
                    )
                }
            }
        }
    }

}
