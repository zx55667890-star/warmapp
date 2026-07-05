package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val leftGlow = Brush.radialGradient(
        colors = listOf(Color(0xFF361C0A), Color.Transparent),
        center = Offset(0f, size.height),
        radius = size.width * 0.9f
    )
    val rightGlow = Brush.radialGradient(
        colors = listOf(Color(0xFF361C0A), Color.Transparent),
        center = Offset(size.width, size.height),
        radius = size.width * 0.9f
    )
    drawRect(brush = leftGlow)
    drawRect(brush = rightGlow)
}
