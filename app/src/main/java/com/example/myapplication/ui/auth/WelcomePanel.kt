package com.example.myapplication.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomePanel(
    isLoading: Boolean,
    agreed: Boolean,
    onAgreedChange: (Boolean) -> Unit,
    onGoogleSignIn: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onSkip: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = "Logo",
            tint = Color(0xFF6B8EFF),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("AppName", color = Color(0xFF6B8EFF), fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF444444))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("Google 登入", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFF444444))
        ) {
            Text("密碼登入", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFF444444))
        ) {
            Text("註冊", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onAgreedChange(!agreed) }
        ) {
            Checkbox(
                checked = agreed,
                onCheckedChange = onAgreedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6B8EFF),
                    uncheckedColor = Color.Gray
                )
            )
            Text("我已閱讀並同意 ", color = Color(0xFF888888), fontSize = 12.sp)
            Text(
                text = "使用者協議",
                color = Color(0xFF6B8EFF),
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onTermsClick)
            )
            Text(" 與 ", color = Color(0xFF888888), fontSize = 12.sp)
            Text(
                text = "隱私政策",
                color = Color(0xFF6B8EFF),
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onPrivacyClick)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "略過直接開始",
            color = Color(0xFF555555),
            fontSize = 14.sp,
            modifier = Modifier.clickable { onSkip() }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
