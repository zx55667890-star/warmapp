package com.example.myapplication.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.AppColors
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraControlButtons(
    cameraVm: CameraViewModel,
    uiState: CameraUiState,
    mainExecutor: Executor,
    context: android.content.Context,
    onImageSavedError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!uiState.isRecording) {
            IconButton(
                onClick = { cameraVm.toggleFlashMode() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = when (uiState.flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> Icons.Filled.FlashOff
                        ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
                        else -> Icons.Filled.FlashAuto
                    },
                    contentDescription = "閃光燈",
                    tint = if (uiState.flashMode == ImageCapture.FLASH_MODE_OFF)
                        AppColors.TextWhite
                    else
                        AppColors.StatusPending
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .pointerInput(uiState.isRecording) {
                    detectTapGestures(
                        onTap = {
                            if (uiState.isRecording) {
                                cameraVm.stopRecording()
                            } else {
                                val photoFile = File(
                                    context.cacheDir,
                                    "camera_${System.currentTimeMillis()}.jpg"
                                )
                                val outputOptions = ImageCapture.OutputFileOptions
                                    .Builder(photoFile)
                                    .build()
                                cameraVm.imageCapture.takePicture(
                                    outputOptions, mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(
                                            output: ImageCapture.OutputFileResults
                                        ) {
                                            cameraVm.onPhotoCaptured(
                                                Uri.fromFile(photoFile)
                                            )
                                        }
                                        override fun onError(
                                            exception: ImageCaptureException
                                        ) {
                                            onImageSavedError()
                                        }
                                    }
                                )
                            }
                        },
                        onLongPress = {
                            if (!uiState.isRecording) {
                                val file = File(
                                    context.cacheDir,
                                    "video_${System.currentTimeMillis()}.mp4"
                                )
                                val outputOptions = FileOutputOptions
                                    .Builder(file)
                                    .build()
                                val rec = cameraVm.videoCapture.output
                                    .prepareRecording(context, outputOptions)
                                    .apply {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            withAudioEnabled()
                                        }
                                    }
                                    .start(mainExecutor) { event ->
                                        if (event is VideoRecordEvent.Finalize &&
                                            !event.hasError()
                                        ) {
                                            cameraVm.onVideoFinalized(
                                                Uri.fromFile(file)
                                            )
                                        } else if (event is VideoRecordEvent.Finalize &&
                                            event.hasError()
                                        ) {
                                            onImageSavedError()
                                        }
                                    }
                                cameraVm.activeRecording = rec
                                cameraVm.startRecording()
                                cameraVm.startTimer()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (uiState.isRecording) 56.dp else 72.dp)
                    .background(
                        color = if (uiState.isRecording)
                            AppColors.StatusError
                        else
                            AppColors.TextWhite,
                        shape = if (uiState.isRecording)
                            RoundedCornerShape(8.dp)
                        else
                            CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!uiState.isRecording) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(
                                AppColors.DarkBackground.copy(alpha = 0.1f),
                                CircleShape
                            )
                    )
                }
            }
        }

        if (!uiState.isRecording) {
            IconButton(
                onClick = { cameraVm.toggleLensFacing() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Cameraswitch,
                    contentDescription = "切換鏡頭",
                    tint = AppColors.TextWhite
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}
