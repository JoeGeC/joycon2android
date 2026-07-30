package com.joegec.joycon2android.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimens {
    val screenPaddingHorizontal = 16.dp
    val screenPaddingVertical = 16.dp
    val cardPadding = 16.dp
    val cardCorner = 20.dp
    val cardBorderWidth = 2.dp
    val cardBorderAlpha = 1f
    val sectionSpacing = 14.dp
    val elementSpacing = 8.dp
    val dualJoyconGap = 10.dp

    val buttonCorner = 12.dp
    val controllerButtonCorner = 6.dp // tighter corner for the on-controller button glyphs
    val buttonHeight = 44.dp
    val buttonHorizontalPadding = 8.dp
    val buttonHeightLarge = 52.dp
    val minTouchTarget = 48.dp // floor for a comfortable tap area, independent of the drawn size
    val shoulderButtonHeight = 30.dp
    val railButtonHeight = 26.dp
    val smallButtonHeight = 30.dp
    val smallButtonWidth = 48.dp
    val dpadSize = 46.dp
    val faceButtonSize = 46.dp
    val iconButtonSize = 36.dp
    val iconSizeSmall = 18.dp
    val iconSizeMedium = 20.dp
    val progressIndicatorSmall = 14.dp

    val stickCanvasSize = 110.dp
    val sidewaysStickSize = 100.dp
    val sidewaysDpadSize = 38.dp
    val sidewaysFaceSize = 38.dp
    val sidewaysShoulderWidth = 36.dp
    val sidewaysShoulderGap = 4.dp
    val sidewaysContentGap = 10.dp
    val stickDotRadius = 8f
    val stickRingStroke = 2f
    val stickRingStrokePressed = 3f
    val crosshairStroke = 1f
    val stickIdleRingAlpha = 0.35f // idle stick ring: a dim wash of the controller colour
    val stickValueGap = 6.dp
    val stickAxisGap = 2.dp

    val legendOverhang = 8.dp
    val legendInset = 12.dp
    val legendLabelPadding = 6.dp
    val legendContentVertical = 10.dp

    val guideTableRowGap = 2.dp
    val imuAxisGap = 2.dp
    val imuTitleGap = 4.dp
    val imuRowSpacing = 8.dp
    val imuSectionSpacing = 6.dp

    val pillCorner = 20.dp
    val pillPaddingHorizontal = 10.dp
    val pillPaddingVertical = 4.dp

    val viewTogglePadding = 4.dp
    val viewToggleCorner = 10.dp
    val compactRowPaddingHorizontal = 16.dp
    val compactRowPaddingVertical = 12.dp
    val compactControllerGap = 4.dp
    val compactChipPaddingHorizontal = 8.dp
    val compactChipPaddingVertical = 6.dp

    val batteryIconWidth = 18.dp
    val batteryIconHeight = 10.dp
    val batteryIconCapWidth = 2.dp
    val batteryIconCorner = 2.dp
    val batteryIconStroke = 1.dp
    val batteryIconTextGap = 5.dp

    val headerLogoSize = 44.dp
    val statusDotSize = 7.dp
    val statusDotGap = 6.dp

    // Glyph and telemetry sizes, tuned to the controller graphics they sit on rather than to a
    // text-hierarchy step. UI text goes through MaterialTheme.typography; telemetry readouts through
    // AppType.telemetry, which these size.
    val fontSizeButton = 14.sp // on-controller button label (SmallButton)
    val fontSizeSmall = 10.sp // on-controller rail label + code/table telemetry
    val fontSizeLabel = 9.sp // IMU / stick coordinate telemetry
    val fontSizeDpad = 12.sp
    val fontSizeFace = 16.sp
    val fontSizeShoulder = 12.sp
    val fontSizeBattery = 11.sp
}
