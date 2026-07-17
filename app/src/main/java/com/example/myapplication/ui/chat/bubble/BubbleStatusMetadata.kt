package com.example.myapplication.ui.chat.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BubbleStatusMetadata(
    timeText: String?,
    isReadByRecipient: Boolean,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {
    if (isMine) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier.padding(end = 8.dp, bottom = 2.dp)
        ) {
            val isDark = isSystemInDarkTheme()
            Box(
                modifier = Modifier
                    .size(if (isReadByRecipient) 5.dp else 4.dp)
                    .background(
                        color = if (isReadByRecipient) {
                            if (isDark) Color(0xFF80CBC4) else Color(0xFF04C9A0)
                        } else {
                            if (isDark) Color(0xFF555555) else Color(0xFFD0D0D0)
                        },
                        shape = CircleShape
                    )
            )
            if (timeText != null) {
                Text(
                    text = timeText,
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                    lineHeight = 12.sp
                )
            }
        }
    } else if (timeText != null) {
        Text(
            text = timeText,
            fontSize = 10.sp,
            color = if (isSystemInDarkTheme()) Color(0xFF888888) else Color(0xFF999999),
            modifier = modifier.padding(start = 6.dp, bottom = 2.dp)
        )
    }
}
