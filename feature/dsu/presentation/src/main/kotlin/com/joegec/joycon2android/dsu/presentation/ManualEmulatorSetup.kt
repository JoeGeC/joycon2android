package com.joegec.joycon2android.dsu.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.ui.components.CopyableCode
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun ManualEmulatorSetup(address: String?) {
    Column {
        GuideStep(stringResource(R.string.dsu_setup_body))
        Spacer(Modifier.height(Dimens.elementSpacing))
        if (address != null) {
            CopyableCode(address)
            Spacer(Modifier.height(Dimens.elementSpacing))
        }
        ExpandableInfoSection(stringResource(R.string.dsu_dolphin_emulator_name)) {
            DolphinManualSteps()
        }
        ExpandableInfoSection(stringResource(R.string.dsu_eden_emulator_name)) {
            EdenManualSteps()
        }
    }
}

@Composable
private fun EdenManualSteps() {
    Column {
        GuideStep(stringResource(R.string.dsu_eden_servers))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_eden_motion))
    }
}

@Composable
private fun DolphinManualSteps() {
    Column {
        GuideStep(stringResource(R.string.dsu_dolphin_android_intro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        CopyableCode(stringResource(R.string.dsu_dolphin_ini))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_dolphin_android_outro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_dolphin_mapping))
    }
}
