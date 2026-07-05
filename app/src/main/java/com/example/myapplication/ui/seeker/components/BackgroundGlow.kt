package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val glowColor = Color(0xFF101B3D)

    val leftGlow = Brush.radialGradient(
        colors = listOf(glowColor, Color.Transparent),
        center = Offset(-size.width * 0.6f, size.height * 1.1f),
        radius = size.width * 1.8f
    )

    val rightGlow = Brush.radialGradient(
        colors = listOf(glowColor, Color.Transparent),
        center = Offset(size.width * 1.6f, size.height * 1.1f),
        radius = size.width * 1.8f
    )

    val baseGlow = Brush.verticalGradient(
        colors = listOf(Color.Transparent, glowColor.copy(alpha = 0.5f), glowColor),
        startY = size.height * 0.7f,
        endY = size.height
    )

    drawRect(brush = leftGlow)
    drawRect(brush = rightGlow)
    drawRect(brush = baseGlow)
}
