package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** The accented current value every dropdown in the app opens from. */
@Composable
internal fun DropdownTrigger(label: String, subLabel: String?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .clickable(onClick = onClick)
            .heightIn(min = Dimens.minTouchTarget)
            .padding(horizontal = Dimens.emulatorPickerPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Accent, style = MaterialTheme.typography.labelMedium)
            subLabel?.let { Text(it, color = TextDim, style = MaterialTheme.typography.labelSmall) }
        }
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Accent)
    }
}
