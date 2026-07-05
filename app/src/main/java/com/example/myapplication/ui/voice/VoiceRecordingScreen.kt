package com.example.myapplication.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VoiceRecordingScreen(
    onDismiss: () -> Unit,
    onVoiceRecorded: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: VoiceRecordingViewModel = viewModel()
    val isRecording by vm.isRecording.collectAsStateWithLifecycle()
    val elapsedSeconds by vm.elapsedSeconds.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    DisposableEffect(Unit) {
        onDispose { vm.release() }
    }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                if (isRecording) "錄音中  ${"%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)}"
                else "點選以進行錄音",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(if (isRecording) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) Color(0xFFFF4444) else Color.White.copy(alpha = 0.15f)
                    )
                    .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable {
                        if (!hasPermission) {
                            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@clickable
                        }
                        if (isRecording) {
                            val path = vm.stopRecording()
                            path?.let { onVoiceRecorded(it) }
                            onDismiss()
                        } else {
                            vm.startRecording(context)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!isRecording) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFF4444), CircleShape)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                "取消",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                modifier = Modifier.clickable { onDismiss() }
            )

            Spacer(Modifier.weight(1f))
        }
    }
}
