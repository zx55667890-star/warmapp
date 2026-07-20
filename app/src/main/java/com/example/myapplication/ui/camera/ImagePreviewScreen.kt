package com.example.myapplication.ui.camera

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.util.MediaMetadataHelper

@Composable
fun ImagePreviewScreen(
    fileUri: Uri,
    isVideo: Boolean,
    onSend: () -> Unit,
    onDiscard: () -> Unit
) {
    var showVideoPlayer by remember { mutableStateOf(false) }
    var isVideoActive by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        if (isVideo) {
            if (showVideoPlayer) {
                VideoPreviewPlayer(
                    fileUri = fileUri,
                    onActiveChanged = { isVideoActive = it }
                )
            } else {
                var videoThumb by remember { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(fileUri) {
                    videoThumb = MediaMetadataHelper.getVideoFrame(
                        fileUri.toString()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showVideoPlayer = true },
                    contentAlignment = Alignment.Center
                ) {
                    videoThumb?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "影片預覽",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
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
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "點擊預覽影片",
                            color = AppColors.TextWhite.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            AsyncImage(
                model = fileUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (!isVideo || !isVideoActive) {
            CameraPreviewActions(
                onDiscard = onDiscard,
                onSend = onSend,
                bottomPadding = isVideo && showVideoPlayer,
                modifier = Modifier.align(
                    if (maxWidth > maxHeight) Alignment.TopCenter
                    else Alignment.BottomCenter
                )
            )
        }
    }
}
