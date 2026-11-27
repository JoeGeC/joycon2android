package com.joegec.joycon2android.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.joegec.joycon2android.buttonmapping.presentation.LayoutLabels
import com.joegec.joycon2android.ui.theme.Dimens

private val FullLayoutMinSize = 180.dp
private val CompactLayoutMinSize = 100.dp

@Composable
fun HomeWidgetContent(state: HomeWidgetState, labels: LayoutLabels) {
    val smallerSide = LocalSize.current.let { minOf(it.width, it.height) }
    when {
        smallerSide < CompactLayoutMinSize -> TinyLayout(state)
        smallerSide < FullLayoutMinSize -> CompactLayout(state, labels)
        else -> FullLayout(state, labels)
    }
}

@Composable
private fun TinyLayout(state: HomeWidgetState) = WidgetFrame {
    ScanButton(state.scanning, scanAction(LocalContext.current, state.blocker), GlanceModifier.fillMaxSize())
}

@Composable
private fun FullLayout(state: HomeWidgetState, labels: LayoutLabels) = WidgetFrame {
    Header(state.scanning, scanAction(LocalContext.current, state.blocker))
    OpenAppArea(Dimens.legendInset) {
        StatusLine(HomeWidgetStatus.of(state))
        PlayerGrid(state.players)
    }
    PickerRow(state, labels)
}

@Composable
private fun CompactLayout(state: HomeWidgetState, labels: LayoutLabels) = WidgetFrame(Dimens.elementSpacing) {
    val context = LocalContext.current
    SlimScanButton(state.scanning, scanAction(context, state.blocker))
    OpenAppArea(Dimens.elementSpacing) {
        StatusLine(HomeWidgetStatus.of(state))
        PlayerGrid(state.players)
    }
    PickerRow(state, labels, compact = true)
}

