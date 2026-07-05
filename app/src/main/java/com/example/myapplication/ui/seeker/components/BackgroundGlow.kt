package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    drawRect(Color.Black)

    val glow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1A3A6B),
            Color(0xFF0D1F3C),
            Color(0xFF060E1F),
            Color.Transparent
        ),
        center = Offset(
            x = size.width / 2f,
            y = size.height * 0.55f
        ),
        radius = size.height * 0.7f
    )

    drawRect(brush = glow)
}
