package com.example.myapplication.ui.chat.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import com.example.myapplication.ui.seeker.components.AttachmentBottomSheet
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    isChatActive: Boolean,
    showRatingDialog: Boolean,
    onSendMessage: (String) -> Unit,
    onSendImage: (List<Uri>) -> Unit,
    onTypingStatusChange: (Boolean) -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    val hasText = inputText.isNotBlank()
    val focusManager = LocalFocusManager.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) onSendImage(uris)
    }

    if (showAttachSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachSheet = false },
            onGalleryClick = {
                showAttachSheet = false
                imagePickerLauncher.launch("image/*")
            },
            onCameraClick = {
                showAttachSheet = false
                onCameraClick()
            },
            onVoiceClick = {
                showAttachSheet = false
                onMicClick()
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
            .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(32.dp)),
        color = Color(0xFF1A1A1E),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            AnimatedVisibility(
                visible = !isChatActive,
                // ✨ 定義進入動畫：由下往上滑入 + 淡入
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 300),
                    initialOffsetY = { it } // it 代表這個 Card 的完整高度，正數表示從它自身高度的下方滑上來
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),

                // ✨ 定義離開動畫：由上往下滑出 + 淡出
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 300),
                    targetOffsetY = { it } // 往下滑出畫面
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A2A2A)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("對方已離開，對話已結束。", color = Color(0xFFC62828), fontSize = 14.sp)
                    }
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { newValue ->
                    inputText = newValue
                    onTypingStatusChange(newValue.isNotBlank())
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("輸入訊息...", color = Color(0xFF888888)) },
                enabled = isChatActive && !showRatingDialog,
                maxLines = 4,
                minLines = 1,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                            onTypingStatusChange(false)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = Color(0xFF888888),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    IconButton(
                        onClick = { focusManager.clearFocus(); showAttachSheet = true },
                        enabled = isChatActive && !showRatingDialog,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            "+",
                            fontSize = 32.sp,
                            color = if (isChatActive && !showRatingDialog) Color(0xFFCCCCCC) else Color(0xFF555555),
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onSendMessage(inputText)
                            inputText = ""
                            onTypingStatusChange(false)
                        },
                        enabled = hasText && isChatActive && !showRatingDialog,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "傳送",
                            tint = if (hasText) Color(0xFFD4A853) else Color(0xFF555555),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    }
}


