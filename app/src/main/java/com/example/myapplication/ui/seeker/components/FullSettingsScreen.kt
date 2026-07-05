package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullSettingsScreen(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // 使用全螢幕 Dialog 鋪滿，達到全螢幕設定頁面的效果
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // 滿版關鍵
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212) // 設定頁面專用質感極致黑
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. 頂部導覽列 (Top App Bar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "設定",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    // 為了平衡右側空間的偽排版留白
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // 2. 設定選單主體內容 (可捲動)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SettingsGroupHeader("帳戶設定")
                    SettingsItem(title = "個人資料與帳號") {}
                    SettingsItem(title = "訂閱與支付方案") {}

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsGroupHeader("App 設定")
                    SettingsItem(title = "自訂主題外觀", subtitle = "深色模式") {}
                    SettingsItem(title = "通用智慧對話設定") {}
                    SettingsItem(title = "資料與隱私權保護") {}

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsGroupHeader("關於")
                    SettingsItem(title = "檢查版本更新", subtitle = "v1.0.0 (Build 24)") {}
                    SettingsItem(title = "服務條款與隱私聲明") {}

                    Spacer(modifier = Modifier.height(20.dp))

                    // 登出按鈕元件
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF241414)) // 隱約的暗紅警告感
                    ) {
                        Text(
                            text = "登出帳戶",
                            color = Color(0xFFEF4444), // 明亮紅字
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF666666),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1A1A))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = Color(0xFF48484A),
            modifier = Modifier.size(20.dp)
        )
    }
}