package com.example.myapplication.ui.chat.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ChatMessage

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
    val isDark = isSystemInDarkTheme()

    if (msg.replyToId.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .widthIn(max = 140.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isMine) Color(0xFF7CD85A) else if (isDark) Color(0xFF2D2D3A) else Color(0xFFE8E8E8))
                .clickable { onQuoteClick(msg.replyToId) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFF04C9A0))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = msg.replyToText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isMine) Color(0xFF333333) else if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
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
                isDarkTheme = isDark,
                onVideoClick = onVideoClick,
                onLongPress = { if (isMine) onLongPress() },
                modifier = Modifier.width(160.dp).height(160.dp)
            )
            if (isPending) {
                Box(
                    modifier = Modifier.matchParentSize().background(Color(0x80000000)).clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF04C9A0))
                }
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
                isDarkTheme = isDark,
                onImageClick = onImageClick,
                onVideoClick = onVideoClick,
                onLongPress = { if (isMine) onLongPress() }
            )
            if (isPending) {
                Box(
                    modifier = Modifier.matchParentSize().background(Color(0x80000000)).clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF04C9A0))
                }
            }
        }
        if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
    }

    if (msg.text.isNotBlank()) {
        Text(
            text = msg.text,
            color = if (isMine) Color.Black else if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}
