package com.example.myapplication.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatTopBar(
    myRole: String,
    isChatActive: Boolean,
    isDarkTheme: Boolean,
    onEndChat: () -> Unit,
    onBack: () -> Unit,
    opponentNickname: String = if (myRole == "expert") "提問者" else "專家",
    myNickname: String = ""
) {
    Surface(
        color = if (isDarkTheme) Color(0xFF2D2D3A) else Color(0xFFF8F9FA),
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
                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
                if (myNickname.isNotBlank()) {
                    Text(
                        text = "我: $myNickname",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) Color(0xFFAAAAAA) else Color.Gray
                    )
                }
                if (!isChatActive) Text("對話已結束", fontSize = 12.sp, color = Color.Red)
            }
            TextButton(onClick = {
                if (isChatActive) onEndChat()
                else onBack()
            }) {
                Text(
                    if (isChatActive) "結束對話" else "返回",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuestionBanner(
    questionText: String,
    isDarkTheme: Boolean
) {
    if (questionText.isNotBlank()) {
        Surface(
            color = if (isDarkTheme) Color(0xFF3A3A4A) else Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isDarkTheme) Color(0xFF4A4A5A) else Color.White, CircleShape)
                        .border(
                            BorderStroke(1.dp, if (isDarkTheme) Color(0xFF666680) else Color(0xFFE0E0E0)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❓", fontSize = 16.sp, color = if (isDarkTheme) Color.White else Color(0xFF04C9A0))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "問題內容",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) Color(0xFFAAAAAA) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = questionText,
                        fontSize = 14.sp,
                        color = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1565C0),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
