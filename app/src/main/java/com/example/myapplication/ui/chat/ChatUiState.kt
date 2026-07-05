package com.example.myapplication.ui.chat

import com.example.myapplication.data.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val pendingMessages: List<ChatMessage> = emptyList(),
    val isOtherTyping: Boolean = false,
    val isChatActive: Boolean = true,
    val chatEndedByOther: Boolean = false,
    val endChatError: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val showEndConfirmDialog: Boolean = false,
    val showRatingDialog: Boolean = false,
    val ratingScore: Int = 0,
    val ratingComment: String = "",
    val ratingError: String? = null,
    val ratingSubmitting: Boolean = false,
    val fullScreenImageUrls: List<String>? = null,
    val fullScreenImageIndex: Int = 0,
    val fullScreenImageIsCameraCapture: List<Boolean> = emptyList(),
    val videoUrl: String? = null,
    val showOpponentProfile: Boolean = false,
    val opponentRating: Double = 5.0,
    val opponentHelpCount: Long = 0L,
    val isInitialLoading: Boolean = true,
    val replyToMessage: ChatMessage? = null,
    val opponentNickname: String = "",
)
