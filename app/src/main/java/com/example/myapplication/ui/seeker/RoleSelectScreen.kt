package com.example.myapplication.ui.seeker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.seeker.components.DrawerContent
import com.example.myapplication.ui.seeker.components.FullSettingsScreen
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import com.example.myapplication.ui.theme.AppColors

@Composable
fun AnimatedAskerIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "asker")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "float"
    )

    val primaryColor = AppColors.AccentBlue
    val accentColor = AppColors.AccentBlue.copy(alpha = 0.7f)

    Canvas(modifier = modifier) {
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(size.width * 0.2f, size.height * 0.55f),
            size = Size(size.width * 0.5f, size.height * 0.45f),
            cornerRadius = CornerRadius(size.width * 0.25f, size.width * 0.25f)
        )
        drawCircle(
            color = primaryColor,
            radius = size.width * 0.18f,
            center = Offset(size.width * 0.45f, size.height * 0.35f)
        )
        val bubbleX = size.width * 0.6f
        val bubbleY = size.height * 0.10f + floatY
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(bubbleX, bubbleY),
            size = Size(size.width * 0.3f, size.height * 0.22f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawCircle(
            color = accentColor,
            radius = 3.dp.toPx(),
            center = Offset(
                bubbleX + 4.dp.toPx(),
                bubbleY + size.height * 0.22f + 2.dp.toPx()
            )
        )
    }
}

@Composable
fun AnimatedExpertIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "expert")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -4f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "float"
    )
    val rayAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "ray"
    )

    val primaryColor = AppColors.AccentGreen
    val bulbColor = AppColors.StatusPending
    val baseColor = AppColors.TextGray

    Canvas(modifier = modifier) {
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(size.width * 0.2f, size.height * 0.55f),
            size = Size(size.width * 0.5f, size.height * 0.45f),
            cornerRadius = CornerRadius(size.width * 0.25f, size.width * 0.25f)
        )
        drawCircle(
            color = primaryColor,
            radius = size.width * 0.18f,
            center = Offset(size.width * 0.45f, size.height * 0.35f)
        )

        val bulbCX = size.width * 0.75f
        val bulbCY = size.height * 0.25f + floatY
        val bulbR = size.width * 0.12f
        val strokeW = size.width * 0.04f

        drawCircle(
            color = bulbColor.copy(alpha = 0.2f),
            radius = bulbR * 2.2f,
            center = Offset(bulbCX, bulbCY)
        )
        drawCircle(
            color = bulbColor,
            radius = bulbR,
            center = Offset(bulbCX, bulbCY)
        )
        drawRoundRect(
            color = baseColor,
            topLeft = Offset(bulbCX - bulbR * 0.5f, bulbCY + bulbR * 0.8f),
            size = Size(bulbR * 1f, bulbR * 0.5f),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        val rayLen = size.width * 0.07f
        val rayGap = size.width * 0.04f
        val rayColor = bulbColor.copy(alpha = rayAlpha)

        drawLine(rayColor,
            Offset(bulbCX, bulbCY - bulbR - rayGap),
            Offset(bulbCX, bulbCY - bulbR - rayGap - rayLen),
            strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(rayColor,
            Offset(bulbCX - bulbR - rayGap, bulbCY),
            Offset(bulbCX - bulbR - rayGap - rayLen, bulbCY),
            strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(rayColor,
            Offset(bulbCX + bulbR + rayGap, bulbCY),
            Offset(bulbCX + bulbR + rayGap + rayLen, bulbCY),
            strokeWidth = strokeW, cap = StrokeCap.Round)
        val diagOffset = (bulbR + rayGap) * 0.707f
        val diagLen = rayLen * 0.707f
        drawLine(rayColor,
            Offset(bulbCX - diagOffset, bulbCY - diagOffset),
            Offset(bulbCX - diagOffset - diagLen, bulbCY - diagOffset - diagLen),
            strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(rayColor,
            Offset(bulbCX + diagOffset, bulbCY - diagOffset),
            Offset(bulbCX + diagOffset + diagLen, bulbCY - diagOffset - diagLen),
            strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

@Composable
fun RoleSelectScreen(
    onLogout: () -> Unit,
    onAskQuestion: () -> Unit,
    onExpertMode: () -> Unit,
    nickname: String,
    avatarUrl: String? = null
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBackgroundGlow()
            .background(AppColors.DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .height(180.dp)
                    .clickable { onAskQuestion() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedAskerIcon(modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("我有問題", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextWhite)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Card(
                modifier = Modifier
                    .width(140.dp)
                    .height(180.dp)
                    .clickable { onExpertMode() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedExpertIcon(modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("分享經驗", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextWhite)
                }
            }
        }

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { isDrawerOpen = true }
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.width(24.dp).height(2.dp).background(AppColors.TextGray))
                Box(Modifier.width(24.dp).height(2.dp).background(AppColors.TextGray))
            }
        }

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DrawerContent(
                    nickname = nickname,
                    avatarUrl = avatarUrl,
                    onSearch = {},
                    onHistoryItemClick = {},
                    onSettingsClick = { showSettingsScreen = true; isDrawerOpen = false }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isDrawerOpen = false }
                        )
                )
            }
        }
    }

    if (showSettingsScreen) {
        FullSettingsScreen(
            onDismiss = { showSettingsScreen = false },
            onLogoutClick = {
                showSettingsScreen = false
                onLogout()
            }
        )
    }
}
