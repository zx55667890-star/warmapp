package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyListState

@Composable
fun ScrollToBottomButton(
    listState: LazyListState,
    isDarkTheme: Boolean,
    totalMessages: Int,
    isLoadingMore: Boolean = false,
    modifier: Modifier = Modifier,
    onScrollToBottom: () -> Unit
) {
    val isAtBottom by remember { derivedStateOf { !listState.canScrollBackward } }
    val currentIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val currentOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    var prevIndex by remember { mutableIntStateOf(0) }
    var prevOffset by remember { mutableIntStateOf(0) }
    var showScrollToBottom by remember { mutableStateOf(false) }
    var prevTotal by remember { mutableIntStateOf(totalMessages) }
    var cooldownUntil by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isAtBottom, currentIndex, currentOffset, totalMessages) {
        if (totalMessages != prevTotal) {
            showScrollToBottom = false
            cooldownUntil = System.currentTimeMillis() + 800L
            prevTotal = totalMessages
            prevIndex = currentIndex
            prevOffset = currentOffset
            return@LaunchedEffect
        }

        if (System.currentTimeMillis() < cooldownUntil) {
            prevIndex = currentIndex
            prevOffset = currentOffset
            return@LaunchedEffect
        }

        if (isAtBottom) {
            showScrollToBottom = false
        } else {
            val scrolledToNewer = currentIndex < prevIndex || (currentIndex == prevIndex && currentOffset < prevOffset)
            showScrollToBottom = scrolledToNewer
        }
        prevIndex = currentIndex
        prevOffset = currentOffset
    }
    AnimatedVisibility(
        visible = showScrollToBottom,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onScrollToBottom,
            containerColor = if (isDarkTheme) Color(0xFF3A3A4A) else Color.White,
            contentColor = if (isDarkTheme) Color.White else Color(0xFF04C9A0),
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "回到底部",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
