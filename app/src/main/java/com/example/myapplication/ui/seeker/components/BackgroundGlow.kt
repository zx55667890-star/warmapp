package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val glowColor = Color(0xFF142959)

    val baseGlow = Brush.verticalGradient(
        colors = listOf(Color.Transparent, glowColor.copy(alpha = 0.4f), glowColor),
        startY = size.height * 0.75f,
        endY = size.height
    )

    val leftGlow = Brush.radialGradient(
        colors = listOf(glowColor, Color.Transparent),
        center = Offset(-size.width * 0.1f, size.height),
        radius = size.width * 1.3f
    )

    val rightGlow = Brush.radialGradient(
        colors = listOf(glowColor, Color.Transparent),
        center = Offset(size.width * 1.1f, size.height),
        radius = size.width * 1.3f
    )

    drawRect(brush = baseGlow)
    drawRect(brush = leftGlow)
    drawRect(brush = rightGlow)
}
