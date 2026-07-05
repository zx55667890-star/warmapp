package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val glowColor = Color(0xFF0D1B3E)

    val baseGlow = Brush.verticalGradient(
        colors = listOf(Color.Transparent, glowColor),
        startY = size.height * 0.6f,
        endY = size.height
    )

    val unifiedGlow = Brush.radialGradient(
        colors = listOf(glowColor, Color.Transparent),
        center = Offset(size.width / 2f, size.height),
        radius = size.width * 1.2f
    )

    drawRect(brush = baseGlow)
    drawRect(brush = unifiedGlow)
}
