package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    drawRect(Color.Black)

    val verticalTransition = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color(0xFF0A1A35),
            Color(0xFF152D5A)
        ),
        startY = 0f,
        endY = size.height
    )
    drawRect(brush = verticalTransition)

    val bottomGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1E4D8C),
            Color(0xFF132A4F),
            Color.Transparent
        ),
        center = Offset(
            x = size.width / 2f,
            y = size.height + size.height * 0.1f
        ),
        radius = size.width * 1.0f
    )
    drawRect(brush = bottomGlow)
}
