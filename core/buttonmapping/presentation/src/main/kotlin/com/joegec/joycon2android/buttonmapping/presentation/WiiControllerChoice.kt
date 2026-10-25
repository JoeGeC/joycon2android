package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SegmentedChoice
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

/** docs/dsu-motion.md#wii-controller */
@Composable
internal fun WiiControllerChoice(
    side: JoyconSide,
    options: List<WiiController>,
    selected: WiiController,
    onSelect: (WiiController) -> Unit,
) {
    Column(
        Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.featureCardTitleGap),
    ) {
        SegmentedChoice(options.map { it to it.label() }, selected, onSelect)
        AnimatedContent(
            selected,
            transitionSpec = { fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()) },
            label = "description",
        ) { controller ->
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.featureCardTitleGap)) {
                Text(controller.description(), color = TextDim, style = MaterialTheme.typography.bodySmall)
                if (controller == WiiController.SIDEWAYS_REMOTE && side == JoyconSide.RIGHT) {
                    SettingWarning(stringResource(R.string.wii_controller_sideways_remote_warning))
                }
            }
        }
    }
}
