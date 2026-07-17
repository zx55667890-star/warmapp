package com.example.myapplication.ui.chat.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors
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
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        title = {
            Text(
                "${if (role == "expert") "專家" else "提問者"} 資訊",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    nickname,
                    color = AppColors.TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProfileStat(label = "評分", value = "%.1f".format(rating))
                    ProfileStat(label = "協助次數", value = "$helpCount")
                }

                if (role == "expert") {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppColors.BorderGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    val score = helpCount * rating
                    val (title, color) = calculateExpertTitle(score)
                    Text(
                        "稱號",
                        color = AppColors.TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        title,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (experienceText != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AppColors.BorderGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "經驗",
                        color = AppColors.TextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        experienceText,
                        color = AppColors.TextWhite,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                if (experienceDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "上架日期：$experienceDate",
                        color = AppColors.TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentGreen,
                    contentColor = AppColors.DarkBackground
                )
            ) {
                Text("關閉", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column {
        Text(
            label,
            color = AppColors.TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            color = AppColors.TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
