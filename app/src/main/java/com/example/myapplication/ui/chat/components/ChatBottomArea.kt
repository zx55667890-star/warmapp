package com.example.myapplication.ui.chat.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.chat.ChatUiState

@Composable
fun ChatBottomArea(
    uiState: ChatUiState,
    isDarkTheme: Boolean,
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
    ) {
        uiState.replyToMessage?.let { replyMsg ->
            ReplyPreviewBar(
                text = replyMsg.text.ifBlank { if (replyMsg.imageUrls.isNotEmpty() || replyMsg.imageUrl.isNotBlank()) "[圖片]" else if (replyMsg.videoUrl.isNotBlank()) "[影片]" else if (replyMsg.voiceUrl.isNotBlank()) "[語音]" else "" },
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
