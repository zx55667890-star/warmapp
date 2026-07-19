package com.example.myapplication.ui.seeker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun MatchingOverlay(
    onCancel: () -> Unit,
    isPendingAcceptance: Boolean = false
) {
    var dotCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.SurfaceMedium,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = AppColors.AccentGreen,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPendingAcceptance) "等待專家接受" else "配對中請稍候",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AppColors.TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isPendingAcceptance) "已找到專家，等待對方接受連線" else "正在為您尋找合適的專家" + ".".repeat(dotCount),
                fontSize = 14.sp,
                color = AppColors.TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, AppColors.BorderGray
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.TextGray
                )
            ) {
                Text("取消配對", fontWeight = FontWeight.Bold)
            }
        }
    }
}
