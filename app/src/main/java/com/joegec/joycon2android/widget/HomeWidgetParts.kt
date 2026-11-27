package com.joegec.joycon2android.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.glance.ButtonDefaults
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.joegec.joycon2android.MainActivity
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ble.ScanBlocker
import com.joegec.joycon2android.buttonmapping.presentation.LayoutLabels
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.presentation.shortLabel
import com.joegec.joycon2android.buttonmapping.presentation.sessionName
import com.joegec.joycon2android.output.presentation.label
import com.joegec.joycon2android.service.Joycon2Service
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.controllerActiveColor
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent
import com.joegec.joycon2android.ui.theme.TextTitle
import com.joegec.joycon2android.ui.theme.Typography
import com.joegec.joycon2android.ui.theme.WarningText

@Composable
internal fun Header(scanning: Boolean, onScan: Action) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            context.getString(R.string.app_name),
            style = textStyle(TextTitle, Typography.titleSmall.fontSize, FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        ScanButton(scanning, onScan)
    }
}

// Glance's Button keeps a tall minimum height, too tall for a 2x2 widget.
@Composable
internal fun SlimScanButton(scanning: Boolean, onScan: Action) {
    Box(
        GlanceModifier
            .fillMaxWidth()
            .background(Accent)
            .cornerRadius(Dimens.pillCorner)
            .padding(vertical = Dimens.statusLinePadding)
            .clickable(onScan),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            LocalContext.current.getString(if (scanning) R.string.widget_scanning else R.string.widget_scan),
            style = textStyle(TextOnAccent, Typography.labelLarge.fontSize, FontWeight.Medium),
            maxLines = 1,
        )
    }
}

@Composable
internal fun ScanButton(scanning: Boolean, onScan: Action, modifier: GlanceModifier = GlanceModifier) {
    Button(
        text = LocalContext.current.getString(if (scanning) R.string.widget_scanning else R.string.widget_scan),
        onClick = onScan,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorProvider(Accent),
            contentColor = ColorProvider(TextOnAccent),
        ),
    )
}

@Composable
internal fun StatusLine(status: HomeWidgetStatus) {
    val text = statusText(LocalContext.current, status) ?: return
    Text(
        text,
        style = textStyle(if (status.isWarning) WarningText else TextDim, Typography.bodySmall.fontSize),
        maxLines = 2,
    )
}

// RemoteViews can't scroll sideways, and Glance text takes one colour, so players sit in a grid.
@Composable
internal fun PlayerGrid(players: List<WidgetPlayer>) {
    players.chunked(PLAYER_GRID_COLUMNS).forEach { row ->
        Row(GlanceModifier.fillMaxWidth().padding(top = Dimens.pillPaddingVertical)) {
            row.forEach { player -> Box(GlanceModifier.defaultWeight()) { PlayerLabel(player) } }
            repeat(PLAYER_GRID_COLUMNS - row.size) { Box(GlanceModifier.defaultWeight()) {} }
        }
    }
}

private const val PLAYER_GRID_COLUMNS = 3

/** "P1 L/R", each controller's letters in its shell colour, brightened as the app's active controller is. */
@Composable
private fun PlayerLabel(player: WidgetPlayer) {
    val resources = LocalContext.current.resources
    val warn = player.connectionLost
    fun colorOf(shell: Int?) = if (warn) WarningText else controllerActiveColor(shell)
    val size = Typography.bodyMedium.fontSize
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            resources.getString(R.string.widget_player_number, player.body.player.index),
            style = textStyle(if (warn) WarningText else TextBright, size, FontWeight.Medium),
        )
        Spacer(GlanceModifier.width(Dimens.pillPaddingVertical))
        val pair = player.body.side == JoyconSide.DUAL && player.shellColors.size == 2
        if (pair) {
            Text(JoyconSide.LEFT.shortLabel(resources), style = textStyle(colorOf(player.shellColors[0]), size, FontWeight.Medium))
            Text(resources.getString(R.string.widget_pair_separator), style = textStyle(TextDim, size, FontWeight.Medium))
            Text(JoyconSide.RIGHT.shortLabel(resources), style = textStyle(colorOf(player.shellColors[1]), size, FontWeight.Medium))
        } else {
            Text(
                player.body.side.shortLabel(resources),
                style = textStyle(colorOf(player.shellColors.firstOrNull()), size, FontWeight.Medium),
            )
        }
    }
}

@Composable
internal fun PickerRow(state: HomeWidgetState, labels: LayoutLabels, compact: Boolean = false) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxWidth()) {
        PickerCell(
            title = context.getString(R.string.widget_playing).takeUnless { compact },
            value = state.emulatorName ?: state.playing.label(context.resources),
            compact = compact,
            onClick = actionStartActivity(Intent(context, PlayingPickerActivity::class.java)),
        )
        state.layout?.let { layout ->
            Spacer(GlanceModifier.width(Dimens.elementSpacing))
            PickerCell(
                title = context.getString(R.string.widget_layout).takeUnless { compact },
                value = labels.sessionName(layout) ?: context.getString(R.string.widget_layout_custom),
                compact = compact,
                onClick = actionStartActivity(Intent(context, GroupLayoutPickerActivity::class.java)),
            )
        }
    }
}

@Composable
internal fun RowScope.PickerCell(title: String?, value: String, compact: Boolean, onClick: Action) {
    Column(
        GlanceModifier
            .defaultWeight()
            .background(CardBg)
            .cornerRadius(Dimens.buttonCorner)
            .padding(horizontal = Dimens.legendInset, vertical = if (compact) Dimens.pillPaddingVertical else Dimens.elementSpacing)
            .clickable(onClick),
    ) {
        title?.let { Text(it, style = textStyle(TextDim, Typography.bodySmall.fontSize), maxLines = 1) }
        Text(
            value,
            style = textStyle(TextBright, Typography.bodyMedium.fontSize, FontWeight.Medium),
            maxLines = if (compact) 1 else 2,
        )
    }
}

// Without what a scan needs, the app asks for it instead.
internal fun scanAction(context: Context, blocker: ScanBlocker?): Action =
    if (blocker == null) {
        actionStartService(
            Intent(context, Joycon2Service::class.java).setAction(Joycon2Service.ACTION_SCAN),
            isForegroundService = true,
        )
    } else {
        openApp(context)
    }

internal fun openApp(context: Context): Action = actionStartActivity(Intent(context, MainActivity::class.java))

internal fun statusText(context: Context, status: HomeWidgetStatus): String? = when (status) {
    HomeWidgetStatus.NEEDS_PERMISSION -> context.getString(R.string.widget_status_permission)
    HomeWidgetStatus.BLUETOOTH_OFF -> context.getString(R.string.widget_status_bluetooth_off)
    HomeWidgetStatus.LOCATION_OFF -> context.getString(R.string.widget_status_location_off)
    HomeWidgetStatus.NEEDS_SHIZUKU -> context.getString(R.string.widget_status_shizuku)
    HomeWidgetStatus.SCANNING -> context.getString(R.string.widget_status_scanning)
    HomeWidgetStatus.SCANNING_WITH_WAITING -> context.getString(R.string.widget_status_scanning_with_waiting)
    HomeWidgetStatus.WAITING -> context.getString(R.string.widget_status_waiting)
    HomeWidgetStatus.READY -> null
}

internal fun textStyle(color: Color, size: TextUnit, weight: FontWeight = FontWeight.Normal) =
    TextStyle(color = ColorProvider(color), fontSize = size, fontWeight = weight)

@Composable
internal fun WidgetFrame(padding: Dp = Dimens.legendInset, content: @Composable ColumnScope.() -> Unit) {
    Column(
        GlanceModifier
            .fillMaxSize()
            .background(Background)
            .cornerRadius(Dimens.cardCorner)
            .padding(padding),
        content = content,
    )
}

/** The middle of the widget opens the app; [gap] above and below keeps a near-miss on a control from doing it. */
@Composable
internal fun ColumnScope.OpenAppArea(gap: Dp, content: @Composable ColumnScope.() -> Unit) {
    Spacer(GlanceModifier.height(gap))
    // Not a LazyColumn: a list takes every tap over it, even where it's empty.
    Column(
        GlanceModifier.defaultWeight().fillMaxWidth().clickable(openApp(LocalContext.current)),
        content = content,
    )
    Spacer(GlanceModifier.height(gap))
}
