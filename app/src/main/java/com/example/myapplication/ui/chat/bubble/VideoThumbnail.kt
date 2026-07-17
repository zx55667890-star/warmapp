package com.example.myapplication.ui.chat.bubble

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.myapplication.util.MediaMetadataHelper
import com.example.myapplication.util.VideoThumbnailCache

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VideoThumbnail(
    url: String,
    isDarkTheme: Boolean,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    val thumbnail = remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) {
        val cached = VideoThumbnailCache.get(url)
        if (cached != null) {
            thumbnail.value = cached
        } else {
            val frame = MediaMetadataHelper.getVideoFrame(url)
            if (frame != null) {
                VideoThumbnailCache.put(url, frame)
                thumbnail.value = frame
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFE0E0E0))
            .combinedClickable(
                onClick = { onVideoClick(url) },
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        thumbnail.value?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "影片縮圖",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xCC000000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
