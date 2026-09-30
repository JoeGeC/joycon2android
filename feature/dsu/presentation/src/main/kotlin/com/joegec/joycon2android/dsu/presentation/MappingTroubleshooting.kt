package com.joegec.joycon2android.dsu.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun MappingTroubleshooting() {
    Column {
        GuideStep(stringResource(R.string.dsu_mapping_trouble_detect))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_names_intro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        Ds4NameTable()
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_missing))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_second_hand))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_trouble_gamepad))
    }
}

// DSU carries exactly the DS4 button set — these are protocol input names, not UI copy.
// SL/SR/Chat have no DSU slot (see dsu_mapping_missing); Capture rides the touchpad click.
private val DS4_BUTTON_NAMES = listOf(
    "A" to "Circle", "B" to "Cross",
    "X" to "Triangle", "Y" to "Square",
    "L" to "L1", "R" to "R1",
    "ZL" to "L2", "ZR" to "R2",
    "−" to "Share", "+" to "Options",
    "LS" to "L3", "RS" to "R3",
    "Home" to "PS", "Capture" to "Touch Button",
    "D-Pad" to "Pad N/S/E/W",
)

@Composable
private fun Ds4NameTable() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.guideTableRowGap)) {
        DS4_BUTTON_NAMES.chunked(2).forEach { rowPairs ->
            Row {
                rowPairs.forEach { (joycon, ds4) ->
                    Ds4NameCell(joycon, ds4, Modifier.weight(1f))
                }
                if (rowPairs.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Ds4NameCell(joycon: String, ds4: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text(
            joycon,
            color = Color.White,
            style = AppType.telemetry,
            fontSize = Dimens.fontSizeSmall,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
        )
        Text(
            " → $ds4",
            color = TextDim,
            style = AppType.telemetry,
            fontSize = Dimens.fontSizeSmall,
        )
    }
}
