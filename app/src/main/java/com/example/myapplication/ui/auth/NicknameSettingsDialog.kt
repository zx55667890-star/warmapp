package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.common.AuthUtils

@Composable
fun NicknameSettingsDialog(
    userId: String,
    userRepository: UserRepository,
    onDismiss: () -> Unit
) {
    var currentNickname by remember { mutableStateOf("") }
    var newNickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var saved by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        userRepository.getNickname(userId) { nickname ->
            currentNickname = nickname
            newNickname = nickname
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("暱稱設定") },
        text = {
            Column {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = {
                            newNickname = it
                            validationError = AuthUtils.validateNickname(it)
                        },
                        label = { Text("暱稱") },
                        singleLine = true,
                        isError = validationError != null,
                        supportingText = validationError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (validationError == null) {
                                userRepository.setNickname(userId, newNickname)
                                currentNickname = newNickname
                                saved = true
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (saved) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "已儲存",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val err = AuthUtils.validateNickname(newNickname)
                    if (err != null) {
                        validationError = err
                    } else {
                        userRepository.setNickname(userId, newNickname)
                        currentNickname = newNickname
                        saved = true
                    }
                },
                enabled = !isLoading && newNickname.isNotBlank()
            ) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("關閉") }
        }
    )
}
