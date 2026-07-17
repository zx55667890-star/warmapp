package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.theme.AppColors

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    drawRect(AppColors.DarkBackground)

    val glow = Brush.radialGradient(
        colors = listOf(
            AppColors.AccentGreen.copy(alpha = 0.08f),
            Color.Transparent
        ),
        center = Offset(size.width / 2f, size.height * 0.35f),
        radius = size.width * 1.5f
    )
    drawRect(brush = glow)

    val blueGlow = Brush.radialGradient(
        colors = listOf(
            AppColors.AccentBlue.copy(alpha = 0.05f),
            Color.Transparent
        ),
        center = Offset(size.width * 0.8f, size.height * 0.7f),
        radius = size.width * 1.2f
    )
    drawRect(brush = blueGlow)
}
