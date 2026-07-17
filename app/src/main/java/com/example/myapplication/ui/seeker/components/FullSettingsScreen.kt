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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.AppColors

@Composable
fun FullSettingsScreen(
    onDismiss: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.DarkBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                AppColors.SurfaceMedium,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = AppColors.TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "設定",
                        color = AppColors.TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    SettingsGroupHeader("帳戶設定")
                    SettingsItem(title = "個人資料與帳號") {}
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsItem(title = "訂閱與支付方案") {}

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsGroupHeader("App 設定")
                    SettingsItem(title = "自訂主題外觀", subtitle = "深色模式") {}
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsItem(title = "通用智慧對話設定") {}
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsItem(title = "資料與隱私權保護") {}

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsGroupHeader("關於")
                    SettingsItem(title = "檢查版本更新", subtitle = "v1.0.0 (Build 24)") {}
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsItem(title = "服務條款與隱私聲明") {}

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.StatusError.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = "登出帳戶",
                            color = AppColors.StatusError,
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
        color = AppColors.TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
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
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.SurfaceDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AppColors.TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = AppColors.TextGray,
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = AppColors.TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
