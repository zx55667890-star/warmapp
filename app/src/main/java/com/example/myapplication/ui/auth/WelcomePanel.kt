package com.example.myapplication.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun WelcomePanel(
    isLoading: Boolean,
    agreed: Boolean,
    onAgreedChange: (Boolean) -> Unit,
    onGoogleSignIn: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    var animPhase by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(5) { i -> delay(80); animPhase = i + 1 }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOut), RepeatMode.Reverse
        ),
        label = "logoScale"
    )
    val logoGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = EaseInOut), RepeatMode.Reverse
        ),
        label = "logoGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .alpha(logoGlow)
                .background(
                    Brush.radialGradient(
                        listOf(AppColors.AccentGreen.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            AnimatedVisibility(
                visible = animPhase >= 1,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = EaseOutBack))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(logoScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        AppColors.AccentGreen.copy(alpha = 0.12f),
                                        AppColors.AccentBlue.copy(alpha = 0.06f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Logo",
                        tint = AppColors.AccentGreen,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = animPhase >= 2,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Text(
                    text = "AppName",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedVisibility(
                visible = animPhase >= 2,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500)) { it / 3 }
            ) {
                Text(
                    text = "即時配對，解決你的問題",
                    fontSize = 14.sp,
                    color = AppColors.TextGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = animPhase >= 3,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                Button(
                    onClick = onGoogleSignIn,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (!isLoading)
                                    Brush.horizontalGradient(
                                        listOf(AppColors.AccentGradientStart, AppColors.AccentGradientEnd)
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(AppColors.TextMuted, AppColors.TextMuted)
                                    ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp).testTag("googleSignInLoading"),
                                strokeWidth = 2.dp,
                                color = AppColors.DarkBackground
                            )
                        } else {
                            Text(
                                "以 Google 繼續",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.DarkBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = animPhase >= 3,
                enter = fadeIn(tween(400, delayMillis = 80)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppColors.BorderGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextWhite,
                        containerColor = AppColors.SurfaceDark
                    )
                ) {
                    Text("密碼登入", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = animPhase >= 4,
                enter = fadeIn(tween(400, delayMillis = 160)) + slideInVertically(tween(400)) { it / 4 }
            ) {
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "建立新帳號",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = animPhase >= 4,
                enter = fadeIn(tween(400, delayMillis = 200))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onAgreedChange(!agreed) }
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = agreed,
                        onCheckedChange = onAgreedChange,
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.AccentGreen,
                            uncheckedColor = AppColors.TextMuted,
                            checkmarkColor = AppColors.DarkBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("我已閱讀並同意 ", color = AppColors.TextGray, fontSize = 12.sp)
                    Text(
                        text = "使用者協議",
                        color = AppColors.AccentBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable(onClick = onTermsClick)
                    )
                    Text(" 與 ", color = AppColors.TextGray, fontSize = 12.sp)
                    Text(
                        text = "隱私政策",
                        color = AppColors.AccentBlue,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable(onClick = onPrivacyClick)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
