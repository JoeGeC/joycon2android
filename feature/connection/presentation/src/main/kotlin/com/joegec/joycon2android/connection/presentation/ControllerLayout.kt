package com.joegec.joycon2android.connection.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun PlayerControllerLayout(
    held: PlayerState,
    liveInputs: StateFlow<Map<String, JoyconInput>>,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Collected here alone, so a packet recomposes only the controller graphics.
    val inputs by liveInputs.collectAsState()
    val state = held.withLiveInput(inputs)
    val layoutType = when {
        state.hasPro -> LayoutType.PRO
        state.hasFullController -> LayoutType.DUAL
        state.left != null -> LayoutType.LEFT
        state.right != null -> LayoutType.RIGHT
        else -> return
    }

    Crossfade(targetState = layoutType, modifier = modifier, label = "controller") { target ->
        when (target) {
            LayoutType.PRO -> ProControllerLayout(state, onUnassign)
            LayoutType.DUAL -> DualJoyconLayout(state, onUnassign)
            LayoutType.LEFT -> LeftSidewaysLayout(state, onUnassign)
            LayoutType.RIGHT -> RightSidewaysLayout(state, onUnassign)
        }
    }
}

private enum class LayoutType { PRO, DUAL, LEFT, RIGHT }
