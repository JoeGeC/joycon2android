package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun CopyableCode(text: String, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            color = Color.White,
            fontSize = Dimens.fontSizeSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .weight(1f)
                .background(Background, RoundedCornerShape(Dimens.elementSpacing))
                .padding(Dimens.elementSpacing),
        )
        TextButton(onClick = { clipboard.setText(AnnotatedString(text)) }) {
            Text(
                stringResource(R.string.copy_label),
                color = Accent,
                fontSize = Dimens.fontSizeSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
