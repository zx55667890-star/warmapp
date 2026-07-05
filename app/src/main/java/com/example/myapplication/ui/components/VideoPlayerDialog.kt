package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import com.example.myapplication.util.VideoCacheManager
import kotlinx.coroutines.delay

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    return "%02d:%02d".format(totalSec / 60, totalSec % 60)
}

@UnstableApi
@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var restored by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var isDragging by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    player?.let { currentPosition = it.currentPosition }
                    player?.pause()
                }
                Lifecycle.Event.ON_STOP -> {
                    player?.let { currentPosition = it.currentPosition }
                    player?.release()
                    player = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            player?.release()
            player = null
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isDragging) {
                player?.let {
                    currentPosition = it.currentPosition
                    duration = it.duration.coerceAtLeast(0L)
                }
            }
            delay(200)
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    // 修正這裡：改用 DefaultDataSource.Factory，它支援 http 和 file 協議
                    val dataSourceFactory = CacheDataSource.Factory()
                        .setCache(VideoCacheManager.getCache(ctx))
                        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(ctx)) // 改成這行+
                    val exoPlayer = ExoPlayer.Builder(ctx)
                        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                        .build()
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            isBuffering = state == Player.STATE_BUFFERING
                            if (state == Player.STATE_READY) {
                                duration = exoPlayer.duration.coerceAtLeast(0L)
                                if (!restored && currentPosition > 0) {
                                    exoPlayer.seekTo(currentPosition)
                                    restored = true
                                }
                            }
                            if (state == Player.STATE_ENDED) {
                                isPlaying = false
                            }
                        }
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            errorMsg = error.localizedMessage ?: "\u64AD\u653E\u5931\u6557"
                            isBuffering = false
                        }
                    })
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true

                    player = exoPlayer

                    PlayerView(ctx).apply {
                        setPlayer(exoPlayer)
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isBuffering && errorMsg == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color.White) }
            }

            if (errorMsg != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(errorMsg!!, color = Color.White, fontSize = 16.sp) }
            }

            if (!isPlaying && !isBuffering && errorMsg == null && duration > 0) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .clickable {
                            player?.seekTo(0)
                            player?.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).background(Color(0x80000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("\u25B6", color = Color.White, fontSize = 32.sp) }
                }
            }

            if (isPlaying && !isBuffering) {
                Box(
                    modifier = Modifier.fillMaxSize().clickable { player?.pause() }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x80000000))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Slider(
                    value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                    onValueChange = { fraction ->
                        isDragging = true
                        currentPosition = (fraction * duration).toLong()
                    },
                    onValueChangeFinished = {
                        player?.seekTo(currentPosition)
                        isDragging = false
                    },
                    modifier = Modifier.fillMaxWidth().height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF04C9A0),
                        activeTrackColor = Color(0xFF04C9A0),
                        inactiveTrackColor = Color(0x66FFFFFF)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                    Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                }
            }

            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                        .statusBarsPadding()
                        .size(40.dp)
                        .background(Color(0x80000000), CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) { Text("\u2715", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
