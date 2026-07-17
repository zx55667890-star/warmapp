package com.example.myapplication.ui.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@UnstableApi
@Composable
fun VideoPreviewPlayer(
    fileUri: Uri,
    onActiveChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isDragging) {
                exoPlayer?.let {
                    currentPosition = it.currentPosition
                    duration = it.duration.coerceAtLeast(0L)
                }
            }
            delay(200)
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer?.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    val player = ExoPlayer.Builder(ctx).build()
                    player.setMediaItem(MediaItem.fromUri(fileUri))
                    player.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_READY) {
                                duration = player.duration.coerceAtLeast(0L)
                            }
                            if (state == Player.STATE_ENDED) {
                                isPlaying = false
                                currentPosition = duration
                            }
                        }
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                            if (!isDragging) onActiveChanged(playing)
                        }
                    })
                    player.prepare()
                    player.playWhenReady = true
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    exoPlayer = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    exoPlayer?.let {
                        if (it.isPlaying) {
                            it.pause()
                        } else {
                            if (it.playbackState == Player.STATE_ENDED) it.seekTo(0)
                            it.play()
                        }
                    }
                }
        )

        if (!isPlaying && duration > 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(AppColors.DarkBackground.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            val fmt = { ms: Long ->
                "%02d:%02d".format(
                    (ms / 1000).toInt() / 60,
                    (ms / 1000).toInt() % 60
                )
            }
            Slider(
                value = if (duration > 0)
                    (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                else 0f,
                onValueChange = { f ->
                    isDragging = true
                    currentPosition = (f * duration).toLong()
                    onActiveChanged(true)
                },
                onValueChangeFinished = {
                    exoPlayer?.seekTo(currentPosition)
                    isDragging = false
                    onActiveChanged(isPlaying)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.AccentGreen,
                    activeTrackColor = AppColors.AccentGreen,
                    inactiveTrackColor = AppColors.TextWhite.copy(alpha = 0.2f)
                )
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    fmt(currentPosition),
                    color = AppColors.TextWhite,
                    fontSize = 12.sp
                )
                Text(
                    fmt(duration),
                    color = AppColors.TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
