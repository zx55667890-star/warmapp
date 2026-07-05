package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val deepBlue = Color(0xFF032581)
    val black = Color.Black

    drawRect(deepBlue)

    val blackHeight = size.height * 0.65f
    drawRect(
        color = black,
        size = Size(size.width, blackHeight)
    )

    val transitionGradient = Brush.radialGradient(
        colors = listOf(
            black,
            black.copy(alpha = 0.4f),
            Color.Transparent
        ),
        center = Offset(size.width / 2f, blackHeight),
        radius = size.width * 0.7f
    )

    drawRect(brush = transitionGradient)
}
