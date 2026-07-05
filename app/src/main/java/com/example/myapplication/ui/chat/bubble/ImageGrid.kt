package com.example.myapplication.ui.chat.bubble

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

fun isVideoUrl(url: String): Boolean {
    val path = android.net.Uri.parse(url).lastPathSegment?.lowercase() ?: return false
    return path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".3gp")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGrid(
    urls: List<String>,
    isDarkTheme: Boolean,
    onImageClick: (List<String>, Int) -> Unit,
    onVideoClick: (String) -> Unit,
    onLongPress: () -> Unit = {}
) {
    val context = LocalContext.current

    if (urls.size == 1) {
        val singleUrl = urls[0]
        if (isVideoUrl(singleUrl)) {
            VideoThumbnail(
                url = singleUrl,
                isDarkTheme = isDarkTheme,
                onVideoClick = onVideoClick,
                modifier = Modifier
                    .width(160.dp)
                    .height(160.dp)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(singleUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "圖片訊息",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(160.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = { onImageClick(urls, 0) },
                        onLongClick = onLongPress
                    )
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            urls.chunked(2).forEach { rowUrls ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowUrls.forEach { url ->
                        val idx = urls.indexOf(url)
                        val imageModifier = Modifier
                            .width(110.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))

                        if (isVideoUrl(url)) {
                            VideoThumbnail(
                                url = url,
                                isDarkTheme = isDarkTheme,
                                onVideoClick = onVideoClick,
                                modifier = imageModifier
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentScale = ContentScale.Crop,
                                contentDescription = "圖片訊息",
                                modifier = imageModifier.combinedClickable(
                                    onClick = { onImageClick(urls, idx) },
                                    onLongClick = onLongPress
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
