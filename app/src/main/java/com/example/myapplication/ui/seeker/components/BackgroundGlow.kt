package com.example.myapplication.ui.seeker.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color

fun Modifier.drawBackgroundGlow(): Modifier = this.drawBehind {
    val topH = size.height / 3f
    val bottomH = size.height / 3f
    val midH = size.height / 3f

    drawRect(Color.Black)
    drawRect(
        color = Color(0xFF032581),
        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - bottomH),
        size = androidx.compose.ui.geometry.Size(size.width, bottomH)
    )
}
