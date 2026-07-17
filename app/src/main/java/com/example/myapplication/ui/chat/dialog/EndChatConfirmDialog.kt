package com.example.myapplication.ui.chat.dialog

import androidx.compose.foundation.isSystemInDarkTheme
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
fun EndChatConfirmDialog(
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
                "結束對話？",
                color = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
            )
        },
        text = {
            Text(
                "確定要結束這次對話嗎？",
                color = if (isDarkTheme) Color(0xFFAAAAAA) else Color.Gray
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("結束", fontWeight = FontWeight.Bold)
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
