package com.example.myapplication.ui.chat.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.chat.ChatUiState

@Composable
fun ChatBottomArea(
    uiState: ChatUiState,
    isDarkTheme: Boolean = true,
    onSendMessage: (String) -> Unit,
    onSendImage: (List<Uri>) -> Unit,
    onTypingStatusChange: (Boolean) -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    onDismissReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .imePadding()
    ) {
        uiState.replyToMessage?.let { replyMsg ->
            val previewText = when {
                replyMsg.text.isNotBlank() -> replyMsg.text
                replyMsg.imageUrls.isNotEmpty() || replyMsg.imageUrl.isNotBlank() -> "[圖片]"
                replyMsg.videoUrl.isNotBlank() -> "[影片]"
                replyMsg.voiceUrl.isNotBlank() -> "[語音]"
                else -> ""
            }
            ReplyPreviewBar(
                text = previewText,
                isDarkTheme = isDarkTheme,
                onDismiss = onDismissReply
            )
        }
        ChatInputBar(
            isChatActive = uiState.isChatActive,
            showRatingDialog = uiState.showRatingDialog,
            onSendMessage = onSendMessage,
            onSendImage = onSendImage,
            onTypingStatusChange = onTypingStatusChange,
            onCameraClick = onCameraClick,
            onMicClick = onMicClick
        )
    }
}
