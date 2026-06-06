package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.PlayerState

@Composable
internal fun PlayerControllerLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.hasFullController -> DualJoyconLayout(state, onUnassign, modifier)
        state.left != null -> LeftSidewaysLayout(state, onUnassign, modifier)
        state.right != null -> RightSidewaysLayout(state, onUnassign, modifier)
    }
}
