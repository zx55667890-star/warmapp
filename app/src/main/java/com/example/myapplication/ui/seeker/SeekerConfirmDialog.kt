package com.example.myapplication.ui.seeker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SeekerConfirmDialog(
    expertName: String,
    expertText: String,
    expertDate: String,
    onConfirm: () -> Unit,
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
                "專家已接受您的提問",
                color = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
            )
        },
        text = {
            Column {
                Text(
                    "專家: $expertName",
                    color = if (isDarkTheme) Color(0xFFCCCCCC) else Color.DarkGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "經驗: $expertText",
                    color = if (isDarkTheme) Color(0xFFE0E0E0) else Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "上架日期: $expertDate",
                    color = if (isDarkTheme) Color(0xFF888888) else Color.Gray,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("確認")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "取消",
                    color = if (isDarkTheme) Color(0xFF80CBC4) else Color(0xFF2196F3)
                )
            }
        }
    )
}
