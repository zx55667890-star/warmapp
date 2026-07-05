package com.example.myapplication.ui.auth

import androidx.compose.foundation.background
import com.example.myapplication.ui.common.CompactTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

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
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
        }

        Text(
            text = if (uiState.isRegisterMode) "註冊" else "登入",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        CompactTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "電子郵件",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CompactTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "密碼",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (uiState.isRegisterMode) ImeAction.Next else ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!uiState.isRegisterMode) onSubmit(email, password, confirmPassword, nickname, verificationCode) })
        )

        if (uiState.isRegisterMode) {
            Spacer(modifier = Modifier.height(8.dp))

            CompactTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "確認密碼",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactTextField(
                value = nickname,
                onValueChange = { nickname = it },
                placeholder = "暱稱",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompactTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                placeholder = "驗證碼",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit(email, password, confirmPassword, nickname, verificationCode) }),
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF555555)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "傳送驗證碼",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable(onClick = { onSendCode(email) })
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(if (uiState.isRegisterMode) 8.dp else 24.dp))

        Button(
            onClick = { onSubmit(email, password, confirmPassword, nickname, verificationCode) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B8EFF))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(
                    text = if (uiState.isRegisterMode) "註冊" else "登入",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!uiState.isRegisterMode) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onForgotPassword) {
                Text("忘記密碼？", color = Color(0xFF6B8EFF))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
