package com.example.myapplication.ui.chat

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScrollManager(
    listState: LazyListState,
    events: SharedFlow<ChatEvent>,
    messages: List<*>,
    pendingMessages: List<*>,
    isInitialLoading: Boolean,
    isOtherTyping: Boolean,
    isChatActive: Boolean,
    markAllRead: () -> Unit
) {
    var highlightedMsgId by remember { mutableStateOf<String?>(null) }

    val totalItems by remember { derivedStateOf { messages.size + pendingMessages.size } }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            when (event) {
                is ChatEvent.ScrollToBottom -> {
                    if (totalItems > 0) listState.animateScrollToItem(0)
                }
                is ChatEvent.ShowSnackbar -> {}
                is ChatEvent.ChatEndedByOther -> {}
                is ChatEvent.OpenCamera -> {}
                is ChatEvent.OpenVoiceRecorder -> {}
            }
        }
    }

    var previousTotalItems by remember { mutableIntStateOf(totalItems) }

    // 當總訊息數增加時，才自動捲動到最底（即 index 0）
    LaunchedEffect(totalItems) {
        if (totalItems > previousTotalItems) {
            // 如果原本就已經在最底部附近，才自動捲動，避免打斷使用者往回看訊息
            if (listState.firstVisibleItemIndex < 3) {
                listState.animateScrollToItem(0)
            }
        }
        previousTotalItems = totalItems
    }

    val isAtBottom by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }
    LaunchedEffect(isAtBottom, messages.size, pendingMessages.size) {
        if (isAtBottom) markAllRead()
    }

    LaunchedEffect(isOtherTyping) {
        if (isOtherTyping && isChatActive && isAtBottom) {
            if (totalItems > 0) {
                delay(50)
                listState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(highlightedMsgId) {
        if (highlightedMsgId != null) {
            delay(1500L)
            highlightedMsgId = null
        }
    }
}
