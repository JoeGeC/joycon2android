package com.joegec.joycon2android.gamepad.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.gamepad.presentation.R
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** Shown when Shizuku isn't running — the privileged backend the gamepad depends on. */
@Composable
fun ShizukuSetupCard(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(Dimens.cardPadding),
    ) {
        Text(
            stringResource(R.string.shizuku_setup_title),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeBody,
        )
        Spacer(Modifier.height(Dimens.elementSpacing))
        Text(
            stringResource(R.string.shizuku_setup_body),
            color = TextDim,
            fontSize = Dimens.fontSizeSmall,
        )
    }
}
