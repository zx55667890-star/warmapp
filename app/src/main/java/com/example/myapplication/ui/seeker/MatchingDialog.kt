package com.example.myapplication.ui.seeker

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatchingDialog(
    onDismiss: () -> Unit,
    onCancel: () -> Unit = onDismiss,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDarkTheme) Color(0xFF2D2D3A) else Color.White,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(if (isDarkTheme) Color(0xFF3A3A4A) else Color(0xFFF0F0F0), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF04C9A0),
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "配對中...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF04C9A0),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "正在為您尋找合適的專家，請稍候",
                    fontSize = 14.sp,
                    color = if (isDarkTheme) Color(0xFFAAAAAA) else Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkTheme) Color(0xFF80CBC4) else Color(0xFF2196F3)
                )
            ) {
                Text("取消配對")
            }
        }
    )
}
