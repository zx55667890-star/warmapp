package com.example.myapplication.ui.chat.bubble

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.ui.theme.AppColors

@Composable
fun ChatBubble(
    msg: ChatMessage,
    isMine: Boolean,
    timeText: String?,
    isPending: Boolean,
    isReadByRecipient: Boolean,
    onRecall: () -> Unit,
    onReply: () -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onVideoClick: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onQuoteClick: (String) -> Unit = {},
    highlighted: Boolean = false
) {
    val bubbleShape = RoundedCornerShape(16.dp)
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            BubbleStatusMetadata(
                timeText = timeText,
                isReadByRecipient = isReadByRecipient,
                isMine = isMine
            )

            if (!isMine) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(AppColors.AccentBlue, CircleShape)
                        .clip(CircleShape)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (msg.senderRole == "expert") "專" else "問",
                        fontSize = 12.sp,
                        color = AppColors.DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(modifier = Modifier.widthIn(max = 260.dp)) {
                val bubbleBg by animateColorAsState(
                    targetValue = when {
                        highlighted -> AppColors.AccentBlue.copy(alpha = 0.3f)
                        isMine -> AppColors.AccentGreen
                        else -> AppColors.SurfaceLight
                    },
                    animationSpec = tween(600),
                    label = "bubbleBg"
                )
                Box(
                    modifier = Modifier
                        .shadow(elevation = 1.dp, shape = bubbleShape)
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = { showMenu = true })
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .animateContentSize()
                ) {
                    BubbleContent(
                        msg = msg,
                        isMine = isMine,
                        isPending = isPending,
                        onImageClick = onImageClick,
                        onVideoClick = onVideoClick,
                        onQuoteClick = onQuoteClick,
                        onLongPress = { showMenu = true }
                    )
                }

                BubbleContextMenu(
                    expanded = showMenu,
                    msg = msg,
                    isMine = isMine,
                    onDismiss = { showMenu = false },
                    onReply = onReply,
                    onRecall = onRecall
                )
            }
        }
    }
}
