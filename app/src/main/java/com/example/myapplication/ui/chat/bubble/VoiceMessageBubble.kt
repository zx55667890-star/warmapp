package com.example.myapplication.ui.chat.bubble

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.util.MediaMetadataHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoiceMessageBubble(
    voiceUrl: String,
    durationMs: Long = 0L,
    onLongPress: () -> Unit = {},
    isMine: Boolean = true
) {
    var isPlaying by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var duration by remember { mutableLongStateOf(durationMs) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(voiceUrl) {
        if (durationMs <= 0L) {
            duration = MediaMetadataHelper.getDuration(voiceUrl)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                currentPositionMs = player?.currentPosition?.toLong() ?: 0L
                delay(200)
                if (currentPositionMs >= duration) break
            }
        }
    }

    fun releasePlayer(p: android.media.MediaPlayer?) {
        try {
            p?.apply { if (isPlaying) stop(); release() }
        } catch (_: Exception) { }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                player?.pause()
                isPlaying = false
            }
            if (event == Lifecycle.Event.ON_STOP) {
                releasePlayer(player)
                player = null
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            releasePlayer(player)
            player = null
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displaySec = if (isPlaying) {
        ((duration - currentPositionMs) / 1000).toInt().coerceIn(1, 999)
    } else {
        (duration / 1000).toInt().coerceAtLeast(1)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .widthIn(min = 60.dp, max = 150.dp)
            .combinedClickable(
                onClick = {
                    if (isPlaying) {
                        player?.pause()
                        isPlaying = false
                    } else {
                        val p = player ?: try {
                            android.media.MediaPlayer().apply {
                                setDataSource(voiceUrl)
                                setOnPreparedListener { start() }
                                setOnCompletionListener {
                                    isPlaying = false
                                    currentPositionMs = duration
                                }
                                setOnErrorListener { _, _, _ -> true }
                                prepareAsync()
                            }
                        } catch (e: Exception) {
                            null
                        } ?: return@combinedClickable
                        if (!p.isPlaying) {
                            if (p.currentPosition >= p.duration - 100) p.seekTo(0)
                            p.start()
                        }
                        player = p
                        isPlaying = true
                    }
                },
                onLongClick = onLongPress
            )

    ) {
        val waveAnim = remember { Animatable(0f) }
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                waveAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            } else {
                waveAnim.snapTo(0f)
            }
        }
        val waveCount = if (isPlaying) (waveAnim.value * 3.99f).toInt() else 3

        val waveCanvas = @Composable {
            Canvas(modifier = Modifier.size(24.dp, 22.dp).then(if (!isMine) Modifier.scale(-1f, 1f) else Modifier)) {
                val cy = size.height / 2
                val cx = size.width

                val radii = listOf(4.dp.toPx(), 8.dp.toPx(), 12.dp.toPx())
                for (i in 0 until waveCount) {
                    if (i >= radii.size) break
                    val r = radii[i]
                    drawArc(
                        color = Color.Black,
                        startAngle = 135f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(cx - 4.dp.toPx() - r, cy - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        if (isMine) {
            Text("${displaySec}\"", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
            waveCanvas()
        } else {
            waveCanvas()
            Text("${displaySec}\"", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}
