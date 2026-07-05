package com.example.myapplication.ui.chat.dialog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.util.calculateExpertTitle

@Composable
fun OpponentProfileDialog(
    opponentId: String,
    role: String,
    nickname: String,
    rating: Double,
    helpCount: Long,
    experienceText: String?,
    experienceDate: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDarkTheme) Color(0xFF2D2D3A) else Color.White,
        title = {
            Text(
                "${if (role == "expert") "專家" else "提問者"} 資訊",
                color = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
            )
        },
        text = {
            Column {
                Text(
                    "暱稱: $nickname",
                    color = if (isDarkTheme) Color(0xFFE0E0E0) else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "評分: ${"%.1f".format(rating)}",
                    color = if (isDarkTheme) Color(0xFFCCCCCC) else Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "協助次數: $helpCount",
                    color = if (isDarkTheme) Color(0xFFCCCCCC) else Color.DarkGray,
                    fontSize = 14.sp
                )
                if (role == "expert") {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = if (isDarkTheme) Color(0xFF444444) else Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val score = helpCount * rating
                    val (title, color) = calculateExpertTitle(score)
                    Text(
                        "稱號: $title",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                if (experienceText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = if (isDarkTheme) Color(0xFF444444) else Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "經驗",
                        color = if (isDarkTheme) Color(0xFF888888) else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        experienceText,
                        color = if (isDarkTheme) Color(0xFFE0E0E0) else Color.Black,
                        fontSize = 14.sp
                    )
                }
                if (experienceDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "上架日期: $experienceDate",
                        color = if (isDarkTheme) Color(0xFF888888) else Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("關閉")
            }
        }
    )
}
