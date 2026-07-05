package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    drawRect(Color(0xFF000000))

    val glow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF132043),
            Color.Transparent
        ),
        center = Offset(size.width / 2f, size.height + (size.height * 0.15f)),
        radius = size.width * 1.2f
    )

    drawRect(brush = glow)
}
