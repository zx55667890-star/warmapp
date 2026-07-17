package com.example.myapplication.ui.chat.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.ui.theme.AppColors

@Composable
fun BubbleContent(
    msg: ChatMessage,
    isMine: Boolean,
    isPending: Boolean,
    onImageClick: (List<String>, Int) -> Unit,
    onVideoClick: (String) -> Unit,
    onQuoteClick: (String) -> Unit,
    onLongPress: () -> Unit
) {
    if (msg.replyToId.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .widthIn(max = 140.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isMine) AppColors.AccentGreen.copy(alpha = 0.4f)
                    else AppColors.SurfaceDark
                )
                .clickable { onQuoteClick(msg.replyToId) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(AppColors.AccentGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = msg.replyToText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isMine) AppColors.DarkBackground
                        else AppColors.TextGray
            )
        }
    }

    if (msg.voiceUrl.isNotBlank()) {
        VoiceMessageBubble(
            voiceUrl = msg.voiceUrl,
            durationMs = msg.voiceDuration,
            onLongPress = onLongPress,
            isMine = isMine
        )
    }

    if (msg.videoUrl.isNotBlank()) {
        Box {
            VideoThumbnail(
                url = msg.videoUrl,
                isDarkTheme = true,
                onVideoClick = onVideoClick,
                onLongPress = { if (isMine) onLongPress() },
                modifier = Modifier
                    .width(160.dp)
                    .height(160.dp)
            )
            if (isPending) {
                PendingOverlay()
            }
        }
        if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
    }

    val allMediaUrls = mutableListOf<String>()
    if (msg.imageUrls.isNotEmpty()) {
        allMediaUrls.addAll(msg.imageUrls)
    } else if (msg.localImageUrls.isNotEmpty()) {
        allMediaUrls.addAll(msg.localImageUrls)
    } else if (msg.imageUrl.isNotBlank()) {
        allMediaUrls.add(msg.imageUrl)
    }

    if (allMediaUrls.isNotEmpty()) {
        Box {
            ImageGrid(
                urls = allMediaUrls,
                isDarkTheme = true,
                onImageClick = onImageClick,
                onVideoClick = onVideoClick,
                onLongPress = { if (isMine) onLongPress() }
            )
            if (isPending) {
                PendingOverlay()
            }
        }
        if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
    }

    if (msg.text.isNotBlank()) {
        Text(
            text = msg.text,
            color = if (isMine) AppColors.DarkBackground
                    else AppColors.TextWhite,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun PendingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppColors.AccentGreen,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp)
        )
    }
}
