package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent
import com.joegec.joycon2android.ui.theme.TextTitle

@Composable
fun NotificationOnboardingScreen(onAllow: () -> Unit, onNotNow: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.widthIn(max = Dimens.onboardingMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        ) {
            Icon(
                Icons.Filled.BatteryAlert,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(Dimens.iconSizeLarge),
            )
            Text(
                stringResource(R.string.onboarding_notifications_title),
                color = TextTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            OnboardingParagraph(stringResource(R.string.onboarding_notifications_why))
            Spacer(Modifier.height(Dimens.elementSpacing))
            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightLarge),
                shape = RoundedCornerShape(Dimens.buttonCorner),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(
                    stringResource(R.string.onboarding_notifications_allow),
                    color = TextOnAccent,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(onClick = onNotNow, modifier = Modifier.height(Dimens.buttonHeight)) {
                Text(stringResource(R.string.onboarding_notifications_not_now), color = TextDim)
            }
            Text(
                stringResource(R.string.onboarding_notifications_later),
                color = TextDim,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OnboardingParagraph(text: String) {
    Text(text, color = TextDim, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
}
