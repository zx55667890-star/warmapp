package com.example.myapplication.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun ChatTopBar(
    myRole: String,
    isChatActive: Boolean,
    isDarkTheme: Boolean = true,
    onEndChat: () -> Unit,
    onBack: () -> Unit,
    opponentNickname: String = if (myRole == "expert") "提問者" else "專家",
    myNickname: String = ""
) {
    Surface(
        color = AppColors.DarkBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = opponentNickname,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppColors.TextWhite
                )
                if (myNickname.isNotBlank()) {
                    Text(
                        text = "我: $myNickname",
                        fontSize = 12.sp,
                        color = AppColors.TextGray
                    )
                }
                if (!isChatActive) {
                    Text(
                        "對話已結束",
                        fontSize = 12.sp,
                        color = AppColors.StatusError
                    )
                }
            }

            val buttonColor = if (isChatActive) AppColors.StatusError else AppColors.AccentBlue
            TextButton(
                onClick = {
                    if (isChatActive) onEndChat() else onBack()
                }
            ) {
                Text(
                    if (isChatActive) "結束對話" else "返回",
                    color = buttonColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuestionBanner(
    questionText: String,
    isDarkTheme: Boolean = true
) {
    if (questionText.isNotBlank()) {
        Surface(
            color = AppColors.DarkBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(AppColors.SurfaceLight, CircleShape)
                        .border(
                            BorderStroke(1.dp, AppColors.BorderGray),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "問題",
                        tint = AppColors.AccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "問題內容",
                        fontSize = 11.sp,
                        color = AppColors.TextGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = questionText,
                        fontSize = 14.sp,
                        color = AppColors.AccentBlue,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
