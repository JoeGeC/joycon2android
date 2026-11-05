package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens

/** One switch for every player, since DSU runs for the whole game or not at all. */
@Composable
internal fun GameMotionCard(console: Console, on: Boolean, onSetOn: (Boolean) -> Unit) {
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_game_motion),
        description = stringResource(
            if (console == Console.SWITCH_PRO) R.string.controller_mapping_game_motion_set_up
            else R.string.controller_mapping_game_motion_description,
        ),
        checked = on,
        onCheckedChange = onSetOn,
        modifier = Modifier
            .background(CardBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(horizontal = Dimens.cardPadding, vertical = Dimens.legendContentVertical),
    )
}
