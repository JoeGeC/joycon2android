package com.joegec.joycon2android.dsu.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun GuideStep(text: String) {
    Text(text, color = TextDim, style = MaterialTheme.typography.bodySmall)
}
