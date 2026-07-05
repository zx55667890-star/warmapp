package com.example.myapplication.ui.seeker

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.seeker.components.DrawerContent
import com.example.myapplication.ui.seeker.components.FullSettingsScreen
import com.example.myapplication.ui.theme.AppColors
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow

/**
 * 提問者：極簡人物 + 浮動的對話氣泡 (代表提出疑問)
 */
@Composable
fun AnimatedAskerIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "asker")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Canvas(modifier = modifier) {
        val primaryColor = Color(0xFF60A5FA)
        val accentColor = Color(0xFF3B82F6)

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
            center = Offset(bubbleX + 4.dp.toPx(), bubbleY + size.height * 0.22f + 2.dp.toPx())
        )
    }
}

/**
 * 專家：極簡人物 + 呼吸發光的智慧燈泡 (代表解答、靈感、專業)
 */
@Composable
fun AnimatedExpertIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "expert")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    val rayAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ray"
    )

    Canvas(modifier = modifier) {
        val primaryColor = Color(0xFF34D399)
        val bulbColor = Color(0xFFFBBF24)
        val baseColor = Color(0xFF9CA3AF)

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

        drawLine(bulbColor.copy(alpha = rayAlpha), Offset(bulbCX, bulbCY - bulbR - rayGap), Offset(bulbCX, bulbCY - bulbR - rayGap - rayLen), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(bulbColor.copy(alpha = rayAlpha), Offset(bulbCX - bulbR - rayGap, bulbCY), Offset(bulbCX - bulbR - rayGap - rayLen, bulbCY), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(bulbColor.copy(alpha = rayAlpha), Offset(bulbCX + bulbR + rayGap, bulbCY), Offset(bulbCX + bulbR + rayGap + rayLen, bulbCY), strokeWidth = strokeW, cap = StrokeCap.Round)
        val diagOffset = (bulbR + rayGap) * 0.707f
        val diagLen = rayLen * 0.707f
        drawLine(bulbColor.copy(alpha = rayAlpha), Offset(bulbCX - diagOffset, bulbCY - diagOffset), Offset(bulbCX - diagOffset - diagLen, bulbCY - diagOffset - diagLen), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(bulbColor.copy(alpha = rayAlpha), Offset(bulbCX + diagOffset, bulbCY - diagOffset), Offset(bulbCX + diagOffset + diagLen, bulbCY - diagOffset - diagLen), strokeWidth = strokeW, cap = StrokeCap.Round)
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
    var showSettingsScreen by remember { mutableStateOf(false) } // 控制全螢幕設定

    // 💡 調整動畫數值，讓圓角與推開層次極致化
    val scale by animateFloatAsState(targetValue = if (isDrawerOpen) 0.82f else 1f, animationSpec = tween(300), label = "")
    val offsetX by animateDpAsState(targetValue = if (isDrawerOpen) 275.dp else 0.dp, animationSpec = tween(300), label = "")
    val cornerRadius by animateDpAsState(targetValue = if (isDrawerOpen) 40.dp else 0.dp, animationSpec = tween(300), label = "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBackgroundGlow() // 只有這個頁面變藍色發光
            .background(Color(0xFF171717))
    ) {
        // 側邊欄
        DrawerContent(
            nickname = nickname,
            avatarUrl = avatarUrl,
            onSearch = {},
            onHistoryItemClick = {},
            onSettingsClick = { showSettingsScreen = true } // 點擊開啟圖四全螢幕設定
        )

        // 右移的主畫面
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .scale(scale)
                .clip(RoundedCornerShape(cornerRadius))
                // 💡 關鍵：加上微亮邊框，能讓上下圓角的孤度在暗色主題下清晰可見，質感倍增
                .border(
                    width = if (isDrawerOpen) 1.dp else 0.dp,
                    color = Color(0xFF2E2E2E),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
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
                        colors = CardDefaults.cardColors(containerColor = AppColors.DarkSurface)
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
                        colors = CardDefaults.cardColors(containerColor = AppColors.DarkSurface)
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
                        Box(modifier = Modifier.width(24.dp).height(2.dp).background(Color(0xFFCCCCCC)))
                        Box(modifier = Modifier.width(24.dp).height(2.dp).background(Color(0xFFCCCCCC)))
                    }
                }
            }

            // 當側邊欄打開時，覆蓋一層透明觸控層，點擊主畫面任何地方皆可收回側邊欄
            if (isDrawerOpen) {
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

    // 🌟 替換：不使用舊的 BottomSheet，改用全新設計的圖四全螢幕設定畫面
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
