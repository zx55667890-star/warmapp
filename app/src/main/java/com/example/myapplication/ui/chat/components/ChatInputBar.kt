package com.example.myapplication.ui.chat.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.seeker.components.AttachmentBottomSheet
import com.example.myapplication.ui.theme.AppColors

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
            .navigationBarsPadding()
            .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
            .border(0.5.dp, AppColors.GlassStroke, RoundedCornerShape(28.dp)),
        color = AppColors.SurfaceMedium,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            AnimatedVisibility(
                visible = !isChatActive,
                enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
                exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.StatusError.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, AppColors.StatusError.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AppColors.StatusError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "對方已離開，對話已結束。",
                            color = AppColors.StatusError,
                            fontSize = 14.sp
                        )
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
                placeholder = {
                    Text("輸入訊息…", color = AppColors.TextMuted)
                },
                enabled = isChatActive && !showRatingDialog,
                maxLines = 4,
                minLines = 1,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                        onTypingStatusChange(false)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = AppColors.AccentGreen,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    disabledTextColor = AppColors.TextGray
                ),
                leadingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showAttachSheet = true
                        },
                        enabled = isChatActive && !showRatingDialog,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            "+",
                            fontSize = 32.sp,
                            color = if (isChatActive && !showRatingDialog)
                                AppColors.TextGray
                            else
                                AppColors.TextMuted,
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
                            tint = if (hasText)
                                AppColors.AccentOrange
                            else
                                AppColors.TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    }
}
