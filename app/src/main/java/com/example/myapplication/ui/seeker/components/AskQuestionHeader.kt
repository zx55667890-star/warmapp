package com.example.myapplication.ui.seeker.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun AskQuestionHeader(nickname: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AppColors.AccentGreen.copy(alpha = glowAlpha),
                                    AppColors.AccentBlue.copy(alpha = glowAlpha * 0.4f),
                                    AppColors.DarkBackground
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = AppColors.AccentGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "$nickname，今天遇到什麼問題？",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextWhite,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "描述越具體，配對越精準",
                fontSize = 13.sp,
                color = AppColors.TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
