package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import com.joegec.joycon2android.ui.theme.ErrorBg
import com.joegec.joycon2android.ui.theme.ErrorText

@Composable
fun ErrorBox(
    text: String?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        text?.let {
            val shape = RoundedCornerShape(Dimens.buttonCorner)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(ErrorBg)
                    .then(
                        if (onClick != null) Modifier.clickable(onClick = onClick)
                        else Modifier
                    )
                    .padding(Dimens.cardPadding)
            ) {
                Text(it, color = ErrorText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
