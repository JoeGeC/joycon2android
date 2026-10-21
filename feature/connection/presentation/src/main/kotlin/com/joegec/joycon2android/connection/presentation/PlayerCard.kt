package com.joegec.joycon2android.connection.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

private const val FadeOutMillis = 90
private const val FadeInMillis = 210
private const val EnterScale = 0.92f

@Composable
fun PlayerCard(
    playerState: PlayerState,
    liveInputs: StateFlow<Map<String, JoyconInput>>,
    viewMode: ConnectionViewMode,
    onUnassign: (String) -> Unit,
    onRemovePlayer: () -> Unit,
    modifier: Modifier = Modifier,
    detailedModifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = viewMode,
        modifier = modifier,
        transitionSpec = { fadeThrough() },
        contentAlignment = Alignment.TopStart,
        label = "viewMode",
    ) { mode ->
        when (mode) {
            ConnectionViewMode.DETAILED -> PlayerView(
                playerState = playerState,
                liveInputs = liveInputs,
                onUnassign = onUnassign,
                onRemovePlayer = onRemovePlayer,
                modifier = detailedModifier,
            )
            ConnectionViewMode.COMPACT -> CompactPlayerRow(
                playerState = playerState,
                onUnassign = onUnassign,
                onRemovePlayer = onRemovePlayer,
            )
        }
    }
}

private fun fadeThrough(): ContentTransform {
    val afterExit = tween<Float>(FadeInMillis, delayMillis = FadeOutMillis, easing = LinearOutSlowInEasing)
    val enter = fadeIn(afterExit) + scaleIn(afterExit, initialScale = EnterScale)
    val exit = fadeOut(tween(FadeOutMillis))
    return ContentTransform(enter, exit, sizeTransform = SizeTransform(clip = false))
}
