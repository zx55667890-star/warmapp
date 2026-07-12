package com.example.myapplication.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.myapplication.ui.chat.bubble.ChatBubble
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.ui.components.ScrollToBottomButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessageList(
    listState: LazyListState,
    allMessages: List<ChatMessage>,
    confirmedMessagesCount: Int,
    isLoadingMore: Boolean,
    hasMoreMessages: Boolean,
    isInitialLoading: Boolean,
    globalImageUrls: List<String>,
    userId: String,
    isDarkTheme: Boolean,
    isChatActive: Boolean,
    isOtherTyping: Boolean,
    highlightedMsgId: String?,
    onLoadMore: () -> Unit,
    onRecall: (ChatMessage) -> Unit,
    onReply: (ChatMessage) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onVideoClick: (String) -> Unit,
    onAvatarClick: (ChatMessage) -> Unit,
    onQuoteClick: (String) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    var loadCooldown by remember { mutableStateOf(false) }
    LaunchedEffect(isLoadingMore) {
        if (!isLoadingMore) {
            delay(200L)
            loadCooldown = false
        }
    }
    val shouldLoadMore by remember {
        derivedStateOf { listState.firstVisibleItemIndex <= 0 }
    }
    LaunchedEffect(shouldLoadMore, hasMoreMessages, isLoadingMore, loadCooldown) {
        if (shouldLoadMore && hasMoreMessages && !isLoadingMore && !loadCooldown) {
            loadCooldown = true
            onLoadMore()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            val reversedMessages = allMessages.asReversed()
            itemsIndexed(
                items = reversedMessages,
                key = { _, msg -> if (msg.localId.isNotBlank()) msg.localId else msg.id }
            ) { index, msg ->
                val isMine = msg.senderId == userId
                val originalIndex = allMessages.lastIndex - index
                
                // åœ¨åè½‰çš„åˆ—è¡¨ä¸­ï¼Œindex 0 æ˜¯æœ€æ–°çš„è¨Šæ¯ã€‚
                // å› æ­¤ã€Œè¼ƒèˆŠã€çš„è¨Šæ¯åœ¨ reversedMessages[index + 1]
                // ã€Œè¼ƒæ–°ã€çš„è¨Šæ¯åœ¨ reversedMessages[index - 1]
                val prevMsg = if (index < reversedMessages.lastIndex) reversedMessages[index + 1] else null
                val nextMsg = if (index > 0) reversedMessages[index - 1] else null

                val showTime = if (msg.timestamp == 0L) false
                else if (nextMsg == null) true
                else !(nextMsg.senderId == msg.senderId && (nextMsg.timestamp - msg.timestamp) < 60_000L)

                val timeText = if (showTime && msg.timestamp != 0L) {
                    dateFormat.format(Date(msg.timestamp))
                } else null

                val topPad = if (prevMsg?.senderId == msg.senderId) 2.dp else 8.dp

                val isPending = originalIndex >= confirmedMessagesCount
                val isReadByRecipient = remember(msg.readBy, userId) {
                    msg.readBy.keys.any { it != userId }
                }

                Column(modifier = Modifier.padding(top = topPad)) {
                    ChatBubble(
                        msg = msg,
                        isMine = isMine,
                        timeText = timeText,
                        isPending = isPending,
                        isReadByRecipient = isReadByRecipient,
                        onRecall = { onRecall(msg) },
                        onReply = { onReply(msg) },
                        onImageClick = onImageClick,
                        onVideoClick = onVideoClick,
                        onAvatarClick = { onAvatarClick(msg) },
                        onQuoteClick = { replyToId ->
                            val targetIndex = reversedMessages.indexOfFirst { it.id == replyToId }
                            if (targetIndex >= 0) {
                                scope.launch { listState.animateScrollToItem(targetIndex) }
                                onQuoteClick(replyToId)
                            }
                        },
                        highlighted = highlightedMsgId == msg.id
                    )
                }
            }
        }

        if (isChatActive && isOtherTyping) {
            TypingIndicator(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 4.dp)
            )
        }

        ScrollToBottomButton(
            listState = listState,
            isDarkTheme = isDarkTheme,
            totalMessages = allMessages.size,
            isLoadingMore = isLoadingMore,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
            onScrollToBottom = {
                scope.launch {
                    try {
                        val total = listState.layoutInfo.totalItemsCount
                        if (total > 0) {
                            listState.animateScrollToItem(0)
                        }
                    } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;}
                }
            }
        )
        if (isLoadingMore) {
            Box(Modifier.fillMaxWidth().padding(4.dp).align(Alignment.TopCenter), contentAlignment = Alignment.Center) {
                Text("è¼‰å…¥ä¸­...", fontSize = 12.sp, color = if (isDarkTheme) Color(0xFF888888) else Color.Gray)
            }
        }
    }
}

