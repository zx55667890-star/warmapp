package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    drawRect(Color(0xFF2631C9))

    val glow = Brush.radialGradient(
        colors = listOf(
            Color.Black,
            Color.Transparent
        ),
        center = Offset(size.width / 2f, size.height / 2f),
        radius = size.width * 4.0f
    )

    drawRect(brush = glow)
}
