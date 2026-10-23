package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextTitle

@Composable
fun SettingsPanel(
    state: SettingsPanelState,
    onViewModeChange: (ConnectionViewMode) -> Unit,
    onFasterUpdatesToggle: (Boolean) -> Unit,
    onBlockDeviceMotionToggle: (Boolean) -> Unit,
    onShowTipsToggle: (Boolean) -> Unit,
    onLaunchOnMainScreenToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        Text(
            stringResource(R.string.settings_title),
            color = TextTitle,
            style = MaterialTheme.typography.headlineSmall,
        )
        KofiBanner()
        SettingsSection {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
                LayoutSetting(state.viewMode, onViewModeChange)
                SettingSwitch(
                    title = stringResource(R.string.settings_show_tips_title),
                    description = stringResource(R.string.settings_show_tips_description),
                    checked = state.showTips,
                    onCheckedChange = onShowTipsToggle,
                    titleStyle = MaterialTheme.typography.titleMedium,
                )
            }
        }
        if (state.mainScreenLaunchAvailable) {
            SettingsSection {
                SettingSwitch(
                    title = stringResource(R.string.settings_launch_on_main_screen_title),
                    description = stringResource(R.string.settings_launch_on_main_screen_description),
                    checked = state.launchOnMainScreen,
                    onCheckedChange = onLaunchOnMainScreenToggle,
                    titleStyle = MaterialTheme.typography.titleMedium,
                )
            }
        }
        SettingsSection {
            SettingSwitch(
                title = stringResource(R.string.settings_faster_updates_title),
                description = stringResource(R.string.settings_faster_updates_description),
                checked = state.outputSettings.fasterUpdates,
                onCheckedChange = onFasterUpdatesToggle,
                titleStyle = MaterialTheme.typography.titleMedium,
            )
        }
        SettingsSection {
            SettingSwitch(
                title = stringResource(R.string.settings_block_device_motion_title),
                description = stringResource(R.string.settings_block_device_motion_description),
                checked = state.outputSettings.blockDeviceMotion,
                onCheckedChange = onBlockDeviceMotionToggle,
                titleStyle = MaterialTheme.typography.titleMedium,
                warning = shizukuWarning(state),
            )
        }
    }
}

@Composable
private fun shizukuWarning(state: SettingsPanelState): String? =
    if (state.shizukuAvailable) null else stringResource(R.string.settings_needs_shizuku)
