package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.drawBackgroundGlow(): Modifier = this
.fillMaxSize()
.drawBehind {

    val w = size.width
    val h = size.height

    // 🌌 Full bleed base（不要黑色）
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF05070F),
                Color(0xFF08162F),
                Color(0xFF05070F)
            )
        )
    )

    // ✨ Main field glow
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4DA3FF).copy(alpha = 0.28f),
                Color(0xFF7C4DFF).copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.4f),
            radius = size.minDimension * 1.6f
        )
    )

    // 🌊 secondary field (補上下空洞感)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF00D4FF).copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.9f),
            radius = size.maxDimension * 1.4f
        )
    )
}