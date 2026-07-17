package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.common.CompactTextField
import com.example.myapplication.ui.theme.AppColors

@Composable
fun LoginForm(
    uiState: AuthUiState,
    onBack: () -> Unit,
    onSendCode: (String) -> Unit,
    onSubmit: (email: String, password: String, confirmPassword: String, nickname: String, verificationCode: String) -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
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
                    .background(
                        AppColors.SurfaceMedium,
                        shape = RoundedCornerShape(12.dp)
                    )
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
            text = if (uiState.isRegisterMode) "建立帳號" else "歡迎回來",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextWhite,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (uiState.isRegisterMode) "填寫以下資料完成註冊" else "輸入帳號密碼登入",
            fontSize = 14.sp,
            color = AppColors.TextGray
        )

        Spacer(modifier = Modifier.height(36.dp))

        FormSectionLabel("帳號資訊")
        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "電子郵件",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        CompactTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "密碼",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (uiState.isRegisterMode) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (!uiState.isRegisterMode) {
                    onSubmit(email, password, confirmPassword, nickname, verificationCode)
                }
            })
        )

        AnimatedVisibility(
            visible = uiState.isRegisterMode,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
        ) {
            Column {
                Spacer(modifier = Modifier.height(28.dp))
                FormSectionLabel("個人資料")
                Spacer(modifier = Modifier.height(12.dp))

                CompactTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "確認密碼",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                CompactTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "暱稱",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(28.dp))
                FormSectionLabel("驗證")
                Spacer(modifier = Modifier.height(12.dp))

                CompactTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    placeholder = "驗證碼",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        onSubmit(email, password, confirmPassword, nickname, verificationCode)
                    }),
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
            }
        }

        Spacer(modifier = Modifier.weight(1f).defaultMinSize(minHeight = 24.dp))

        Button(
            onClick = { onSubmit(email, password, confirmPassword, nickname, verificationCode) },
            enabled = !uiState.isLoading,
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
                        brush = if (!uiState.isLoading)
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
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.DarkBackground
                    )
                } else {
                    Text(
                        text = if (uiState.isRegisterMode) "完成註冊" else "登入",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!uiState.isLoading) AppColors.DarkBackground else AppColors.TextGray
                    )
                }
            }
        }

        if (!uiState.isRegisterMode) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onForgotPassword) {
                Text(
                    "忘記密碼？",
                    color = AppColors.AccentBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.TextGray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp),
        letterSpacing = 0.5.sp
    )
}
