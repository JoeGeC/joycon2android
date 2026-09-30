package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.joegec.joycon2android.ui.theme.CardBg

// Leaves the screen behind in view; Material still caps the sheet at 360dp.
private const val WidthFraction = 0.9f

// Material only opens drawers from the start edge, so this mirrors the layout direction around it.
@Composable
fun EndDrawer(
    drawerState: DrawerState,
    drawerContent: @Composable ColumnScope.() -> Unit,
    containerColor: Color = CardBg,
    content: @Composable () -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val mirrored = if (direction == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides mirrored) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    drawerState = drawerState,
                    modifier = Modifier.fillMaxWidth(WidthFraction),
                    drawerContainerColor = containerColor,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides direction) {
                        drawerContent()
                    }
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
        }
    }
}
