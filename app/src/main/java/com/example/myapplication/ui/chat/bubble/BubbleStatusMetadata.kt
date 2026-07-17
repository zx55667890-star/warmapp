package com.example.myapplication.ui.chat.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

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
            Box(
                modifier = Modifier
                    .size(if (isReadByRecipient) 5.dp else 4.dp)
                    .background(
                        color = if (isReadByRecipient)
                            AppColors.AccentGreen
                        else
                            AppColors.TextMuted,
                        shape = CircleShape
                    )
            )
            if (timeText != null) {
                Text(
                    text = timeText,
                    fontSize = 10.sp,
                    color = AppColors.TextGray,
                    lineHeight = 12.sp
                )
            }
        }
    } else if (timeText != null) {
        Text(
            text = timeText,
            fontSize = 10.sp,
            color = AppColors.TextGray,
            modifier = modifier.padding(start = 6.dp, bottom = 2.dp)
        )
    }
}
