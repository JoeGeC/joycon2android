package com.joegec.joycon2android.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.buttonmapping.presentation.layoutLabels

class HomeWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as JoyconApplication).container
        val labels = layoutLabels(context.resources)
        provideContent {
            val state by container.homeWidgetState.collectAsState()
            HomeWidgetContent(state, labels)
        }
    }
}
