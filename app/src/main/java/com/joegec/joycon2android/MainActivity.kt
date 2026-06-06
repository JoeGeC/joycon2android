package com.joegec.joycon2android

import android.Manifest
import android.os.Build
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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.all { it }) {
                viewModel.startScan()
            } else {
                viewModel.onPermissionsDenied()
            }
        }

        setContent {
            Joycon2AndroidTheme {
                Surface(Modifier.fillMaxSize(), color = Background) {
                    val state by viewModel.state.collectAsState()
                    JoyconScreen(
                        state = state,
                        onConnect = { permLauncher.launch(requiredPermissions()) },
                        onStop = { viewModel.stop() },
                    )
                }
            }
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
