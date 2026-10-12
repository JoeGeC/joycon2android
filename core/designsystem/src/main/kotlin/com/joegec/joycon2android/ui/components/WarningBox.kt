package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.WarningBg
import com.joegec.joycon2android.ui.theme.WarningText

@Composable
fun WarningBox(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .background(WarningBg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(Dimens.cardPadding)
    ) {
        Text(text, color = WarningText, style = MaterialTheme.typography.bodySmall)
    }
}
