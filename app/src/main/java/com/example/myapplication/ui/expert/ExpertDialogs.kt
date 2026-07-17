package com.example.myapplication.ui.expert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

// ═══════════════════════════════════════════════════
// 有新問題彈窗
// ═══════════════════════════════════════════════════

@Composable
fun ExpertAssignDialog(
    questionText: String,
    userId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        textContentColor = AppColors.TextGray,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            AppColors.AccentGreen.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = AppColors.AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "有新問題！",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    "有人正在等待您的幫助",
                    fontSize = 13.sp,
                    color = AppColors.TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.SurfaceMedium
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AppColors.AccentGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "問題內容",
                                fontSize = 11.sp,
                                color = AppColors.AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = questionText,
                            fontSize = 15.sp,
                            color = AppColors.TextWhite,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentGreen,
                    contentColor = AppColors.DarkBackground
                )
            ) {
                Text("接受問題", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("略過此問題", color = AppColors.TextGray)
            }
        }
    )
}

// ═══════════════════════════════════════════════════
// 等待確認彈窗
// ═══════════════════════════════════════════════════

@Composable
fun ExpertWaitingDialog(
    onCancel: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                CircularProgressIndicator(
                    color = AppColors.AccentGreen,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "等待提問者確認中…",
                    color = AppColors.TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "提問者正在查看您的經驗是否符合他的需求",
                    color = AppColors.TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, AppColors.BorderGray
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.TextGray
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
