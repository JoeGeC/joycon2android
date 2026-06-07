package com.joegec.joycon2android.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.PlayerState

@Composable
internal fun PlayerControllerLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutType = when {
        state.hasFullController -> LayoutType.DUAL
        state.left != null -> LayoutType.LEFT
        state.right != null -> LayoutType.RIGHT
        else -> return
    }

    Crossfade(targetState = layoutType, modifier = modifier, label = "controller") { target ->
        when (target) {
            LayoutType.DUAL -> DualJoyconLayout(state, onUnassign)
            LayoutType.LEFT -> LeftSidewaysLayout(state, onUnassign)
            LayoutType.RIGHT -> RightSidewaysLayout(state, onUnassign)
        }
    }
}

private enum class LayoutType { DUAL, LEFT, RIGHT }
