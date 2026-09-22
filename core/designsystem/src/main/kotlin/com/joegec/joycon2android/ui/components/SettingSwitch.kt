package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.WarningText

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    descriptionColor: Color = TextDim,
    warning: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Dimens.featureCardTitleGap))
            Text(description, color = descriptionColor, style = MaterialTheme.typography.bodySmall)
            warning?.let {
                Spacer(Modifier.height(Dimens.featureCardTitleGap))
                SettingWarning(it)
            }
        }
        Spacer(Modifier.width(Dimens.featureCardSwitchGap))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent),
        )
    }
}

/** A caveat the setting carries, marked so it reads as one rather than as more description. */
@Composable
private fun SettingWarning(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.statusDotGap)) {
        Icon(
            Icons.Filled.WarningAmber,
            contentDescription = stringResource(R.string.setting_warning),
            tint = WarningText,
            modifier = Modifier.size(Dimens.iconSizeTiny),
        )
        Text(text, color = WarningText, style = MaterialTheme.typography.bodySmall)
    }
}
