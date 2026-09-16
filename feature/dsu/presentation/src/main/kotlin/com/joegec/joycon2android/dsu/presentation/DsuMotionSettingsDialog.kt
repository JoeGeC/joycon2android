package com.joegec.joycon2android.dsu.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.WarningText

@Composable
fun DsuMotionSettingsDialog(
    settings: DsuMotionSettings,
    deviceMotionBlockAvailable: Boolean,
    onFastMotionToggle: (Boolean) -> Unit,
    onBlockDeviceMotionToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                stringResource(R.string.dsu_motion_settings_title),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
        },
        text = {
            Column {
                SettingSwitch(
                    title = stringResource(R.string.dsu_fast_motion_title),
                    description = stringResource(R.string.dsu_fast_motion_warning),
                    descriptionColor = WarningText,
                    checked = settings.fastMotion,
                    onCheckedChange = onFastMotionToggle,
                )
                Spacer(Modifier.height(Dimens.sectionSpacing))
                SettingSwitch(
                    title = stringResource(R.string.dsu_block_device_motion_title),
                    description = blockDeviceMotionDescription(deviceMotionBlockAvailable),
                    checked = settings.blockDeviceMotion,
                    onCheckedChange = onBlockDeviceMotionToggle,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsu_motion_settings_done), color = Accent, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
private fun blockDeviceMotionDescription(available: Boolean): String {
    val description = stringResource(R.string.dsu_block_device_motion_description)
    if (available) return description
    return description + "\n" + stringResource(R.string.dsu_block_device_motion_needs_shizuku)
}
