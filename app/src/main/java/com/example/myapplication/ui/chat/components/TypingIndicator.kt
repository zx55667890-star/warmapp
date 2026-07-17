package com.example.myapplication.ui.chat.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "對方正在輸入",
                fontSize = 14.sp,
                color = AppColors.TextGray
            )
            Spacer(modifier = Modifier.width(4.dp))
            (0..2).forEach { i ->
                val infiniteTransition = rememberInfiniteTransition(label = "dot$i")
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -5f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 900
                            0f at 0 using LinearEasing
                            -5f at (200 + i * 150) using LinearEasing
                            0f at (400 + i * 150) using LinearEasing
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dotOffset$i"
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                0, offsetY.dp.roundToPx()
                            )
                        }
                        .background(AppColors.TextGray, CircleShape)
                )
            }
        }
    }
}
