package com.example.myapplication.ui.seeker.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.drawBackgroundGlow(): Modifier {
    val animationProgress by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowProgress"
    )

    return this.drawBehind {
        drawRect(Color.Black)

        val glow = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1A3A6B),
                Color(0xFF0D1F3C),
                Color.Transparent
            ),
            center = Offset(
                x = size.width / 2f,
                y = size.height * 0.6f
            ),
            radius = (size.height * 0.7f) * animationProgress
        )

        drawRect(brush = glow)
    }
}
