package com.example.myapplication.ui.chat.bubble

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.util.MediaMetadataHelper
import com.example.myapplication.util.VideoThumbnailCache

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VideoThumbnail(
    url: String,
    isDarkTheme: Boolean = true,
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
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.SurfaceDark)
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
                .background(
                    AppColors.DarkBackground.copy(alpha = 0.6f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放",
                tint = AppColors.TextWhite,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
