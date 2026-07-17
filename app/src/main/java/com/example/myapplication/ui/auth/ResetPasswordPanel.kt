package com.example.myapplication.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.myapplication.ui.common.CompactTextField
import com.example.myapplication.ui.theme.AppColors

// ═══════════════════════════════════════════════════
// 忘記密碼 — 步驟一：輸入信箱 + 驗證碼
// ═══════════════════════════════════════════════════

@Composable
fun ForgotPasswordPanel(
    uiState: AuthUiState,
    onBack: () -> Unit,
    onSendCode: (String) -> Unit,
    onNext: (email: String, verificationCode: String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var verificationCode by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── 頂部列 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.SurfaceMedium, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = AppColors.TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "重設密碼",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextWhite,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "輸入您的電子郵件，我們將發送驗證碼",
            fontSize = 14.sp,
            color = AppColors.TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "電子郵件",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "輸入電子郵件",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "驗證碼",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = verificationCode,
            onValueChange = { verificationCode = it },
            placeholder = "輸入驗證碼",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onNext(email, verificationCode) }),
            trailingIcon = {
                TextButton(
                    onClick = { onSendCode(email) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "傳送驗證碼",
                        color = AppColors.AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 32.dp))

        GradientButton(
            text = "下一步",
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && verificationCode.isNotBlank() && !uiState.isLoading,
            onClick = { onNext(email, verificationCode) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════
// 重設密碼 — 步驟二：輸入新密碼
// ═══════════════════════════════════════════════════

@Composable
fun NewPasswordForm(
    uiState: AuthUiState,
    onBack: () -> Unit,
    onConfirm: (newPassword: String, confirmNewPassword: String) -> Unit
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.SurfaceMedium, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = AppColors.TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "設定新密碼",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextWhite,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "請輸入新密碼，長度至少 6 個字元",
            fontSize = 14.sp,
            color = AppColors.TextGray
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "新密碼",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = "輸入新密碼",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "確認密碼",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = confirmNewPassword,
            onValueChange = { confirmNewPassword = it },
            placeholder = "再次輸入新密碼",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                onConfirm(newPassword, confirmNewPassword)
            })
        )

        Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 32.dp))

        GradientButton(
            text = "確認修改",
            isLoading = uiState.isLoading,
            enabled = newPassword.isNotBlank() &&
                    confirmNewPassword.isNotBlank() &&
                    !uiState.isLoading,
            onClick = { onConfirm(newPassword, confirmNewPassword) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════
// 共用：漸層主按鈕（登入頁統一風格）
// ═══════════════════════════════════════════════════

@Composable
internal fun GradientButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled)
                        Brush.horizontalGradient(
                            listOf(AppColors.AccentGradientStart, AppColors.AccentGradientEnd)
                        )
                    else
                        Brush.horizontalGradient(
                            listOf(AppColors.TextMuted, AppColors.TextMuted)
                        ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = AppColors.DarkBackground
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) AppColors.DarkBackground else AppColors.TextGray
                )
            }
        }
    }
}
