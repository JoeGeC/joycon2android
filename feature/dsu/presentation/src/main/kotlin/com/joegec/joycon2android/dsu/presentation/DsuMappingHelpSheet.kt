package com.joegec.joycon2android.dsu.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.InfoSheet

@Composable
fun DsuMappingHelpSheet(address: String?, onDismiss: () -> Unit) {
    InfoSheet(title = stringResource(R.string.dsu_manual_setup_title), onDismiss = onDismiss) {
        ManualEmulatorSetup(address)
        ExpandableInfoSection(stringResource(R.string.dsu_mapping_trouble_title)) {
            MappingTroubleshooting()
        }
    }
}
