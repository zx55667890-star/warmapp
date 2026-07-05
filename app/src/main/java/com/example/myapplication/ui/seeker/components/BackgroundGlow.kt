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
        drawRect(Color(0xFF0D1F3C))

        val blackOverlay = Brush.radialGradient(
            colors = listOf(
                Color.Black,
                Color.Black.copy(alpha = 0.9f),
                Color.Transparent
            ),
            center = Offset(
                x = size.width / 2f,
                y = -size.height * 0.1f
            ),
            radius = (size.height * 1.1f) * animationProgress
        )

        drawRect(brush = blackOverlay)
    }
}
