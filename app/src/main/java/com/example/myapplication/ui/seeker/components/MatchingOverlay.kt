package com.example.myapplication.ui.seeker.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AppColors

@Composable
fun MatchingOverlay(
    onCancel: () -> Unit,
    isPendingAcceptance: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "matching_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceLight)
                    .alpha(pulseAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isPendingAcceptance)
                    stringResource(R.string.matching_pending_title)
                else
                    stringResource(R.string.matching_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPendingAcceptance)
                    stringResource(R.string.matching_pending_subtitle)
                else
                    stringResource(R.string.matching_subtitle),
                fontSize = 14.sp,
                color = AppColors.TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.StatusError.copy(alpha = 0.15f),
                    contentColor = AppColors.StatusError
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(160.dp)
                    .height(44.dp)
            ) {
                Text(
                    text = stringResource(R.string.matching_cancel),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
