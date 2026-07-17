package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.common.AuthUtils
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun NicknameSettingsDialog(
    userId: String,
    userRepository: UserRepository,
    onDismiss: () -> Unit
) {
    var currentNickname by remember { mutableStateOf("") }
    var newNickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        val nickname = userRepository.getNickname(userId)
        currentNickname = nickname
        newNickname = nickname
        isLoading = false
    }

    LaunchedEffect(saved) {
        if (saved) {
            delay(1200)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        textContentColor = AppColors.TextGray,
        title = {
            Text("暱稱設定", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.AccentGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = {
                            newNickname = it
                            validationError = AuthUtils.validateNickname(it)
                            saved = false
                        },
                        label = { Text("暱稱") },
                        singleLine = true,
                        isError = validationError != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.TextWhite,
                            unfocusedTextColor = AppColors.TextWhite,
                            focusedBorderColor = AppColors.AccentGreen,
                            unfocusedBorderColor = AppColors.BorderGray,
                            cursorColor = AppColors.AccentGreen,
                            focusedLabelColor = AppColors.AccentGreen,
                            unfocusedLabelColor = AppColors.TextGray,
                            focusedContainerColor = AppColors.SurfaceMedium,
                            unfocusedContainerColor = AppColors.SurfaceMedium,
                            errorBorderColor = AppColors.StatusError,
                            errorLabelColor = AppColors.StatusError,
                            errorCursorColor = AppColors.StatusError
                        ),
                        supportingText = validationError?.let {
                            {
                                Text(
                                    it,
                                    color = AppColors.StatusError,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (validationError == null && newNickname.isNotBlank() && !isSaving) {
                                isSaving = true
                                userRepository.setNickname(userId, newNickname)
                                currentNickname = newNickname
                                isSaving = false
                                saved = true
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(visible = saved) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.StatusSuccessBg
                        ) {
                            Text(
                                "已儲存，即將關閉",
                                color = AppColors.StatusSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val err = AuthUtils.validateNickname(newNickname)
                    if (err != null) {
                        validationError = err
                    } else {
                        isSaving = true
                        userRepository.setNickname(userId, newNickname)
                        currentNickname = newNickname
                        isSaving = false
                        saved = true
                    }
                },
                enabled = !isLoading && !isSaving && newNickname.isNotBlank() && !saved,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentGreen,
                    contentColor = AppColors.DarkBackground,
                    disabledContainerColor = AppColors.AccentGreen.copy(alpha = 0.3f)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.DarkBackground
                    )
                } else {
                    Text("儲存", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("關閉", color = AppColors.TextGray)
            }
        }
    )
}
