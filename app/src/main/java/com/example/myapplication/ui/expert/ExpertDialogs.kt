package com.example.myapplication.ui.expert

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpertAssignDialog(
    questionText: String,
    userId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = { onReject() },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDarkTheme) Color(0xFF1C1B2E) else Color.White,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x2204C9A0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 26.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("有新問題！", fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color(0xFF333333), fontSize = 20.sp)
            }
        },
        text = {
            Column {
                Text(
                    "有人正在等待您的幫助",
                    fontSize = 13.sp,
                    color = if (isDarkTheme) Color(0xFF9090A8) else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF252438) else Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF04C9A0), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("問題內容", fontSize = 11.sp, color = Color(0xFF04C9A0), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = questionText, fontSize = 15.sp, color = if (isDarkTheme) Color(0xFFE0E0E0) else Color(0xFF333333), lineHeight = 22.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAccept()
                    Toast.makeText(context, "已接受問題！", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF04C9A0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("接受問題", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("略過此問題", color = if (isDarkTheme) Color(0xFF666680) else Color.Gray)
            }
        }
    )
}

@Composable
fun ExpertWaitingDialog(
    onCancel: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDarkTheme) Color(0xFF1C1B2E) else Color.White,
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF04C9A0))
                Spacer(modifier = Modifier.height(24.dp))
                Text("等待提問者確認中...", color = if (isDarkTheme) Color.White else Color(0xFF333333), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("提問者正在查看您的經驗是否符合他的需求", color = if (isDarkTheme) Color(0xFF9090A8) else Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDarkTheme) Color(0xFF80CBC4) else Color(0xFF2196F3)
                    )
                ) {
                    Text("取消")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
