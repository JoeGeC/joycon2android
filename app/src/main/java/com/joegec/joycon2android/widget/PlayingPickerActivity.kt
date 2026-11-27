package com.joegec.joycon2android.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.output.presentation.OutputViewModel
import com.joegec.joycon2android.output.presentation.PlayingPickerDialog
import com.joegec.joycon2android.outputViewModel
import com.joegec.joycon2android.ui.theme.Joycon2AndroidTheme

/** Opened from the home-screen widget, over the home screen. */
class PlayingPickerActivity : ComponentActivity() {

    private val outputViewModel: OutputViewModel by viewModels {
        viewModelFactory { initializer { (application as JoyconApplication).container.outputViewModel() } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Joycon2AndroidTheme {
                val selection by outputViewModel.selection.collectAsState()
                PlayingPickerDialog(
                    selection = selection,
                    emulators = outputViewModel.emulators,
                    onSelect = { playing, emulatorId ->
                        outputViewModel.select(playing, emulatorId).invokeOnCompletion { finish() }
                    },
                    onDismiss = ::finish,
                )
            }
        }
    }
}
