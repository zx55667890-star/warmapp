package com.example.myapplication.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Size
import android.view.WindowManager
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.video.FileOutputOptions
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.util.concurrent.Executor
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recording
import android.view.MotionEvent

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Uri, Boolean) -> Unit,
    onDismiss: () -> Unit,
    preWarmFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    DisposableEffect(Unit) {
        activity?.let { enterFullScreen(it) }
        onDispose { activity?.let { exitFullScreen(it) } }
    }

    if (!hasCameraPermission) {
        LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.CAMERA) }
    }

    if (hasCameraPermission) {
        val lifecycleOwner = LocalLifecycleOwner.current
        @Suppress("DEPRECATION")
        val cameraProviderFuture = remember { preWarmFuture ?: ProcessCameraProvider.getInstance(context) }
        val mainExecutor: Executor = remember { ContextCompat.getMainExecutor(context) }
        val cameraVm: CameraViewModel = viewModel()

        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        var camera by remember { mutableStateOf<Camera?>(null) }
        val cameraUiState by cameraVm.uiState.collectAsStateWithLifecycle()
        var focusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
        val density = LocalDensity.current

        LaunchedEffect(Unit) { cameraVm.resetState() }

        var cameraReady by remember { mutableStateOf(false) }

        LaunchedEffect(cameraUiState.lensFacing, previewView) {
            val pv = previewView ?: return@LaunchedEffect
            cameraReady = false
            val provider = withContext(Dispatchers.IO) { cameraProviderFuture.get() }
            provider.unbindAll()
            val selector = CameraSelector.Builder().requireLensFacing(cameraUiState.lensFacing).build()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayRot: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                displayRot = context.display!!.rotation
            } else {
                @Suppress("DEPRECATION")
                displayRot = windowManager.defaultDisplay.rotation
            }
            @Suppress("DEPRECATION")
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(displayRot)
                .build().apply {
                setSurfaceProvider(pv.surfaceProvider)
            }
            cameraVm.imageCapture.flashMode = cameraUiState.flashMode
            cameraVm.videoCapture.targetRotation = displayRot
            cameraVm.imageCapture.targetRotation = displayRot
            camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, cameraVm.imageCapture, cameraVm.videoCapture)
            cameraReady = true
        }

        if (cameraUiState.cameraState == CameraCaptureState.PREVIEW && cameraUiState.capturedFileUri != null) {
            cameraUiState.capturedFileUri?.let { uri ->
                ImagePreviewScreen(
                    fileUri = uri,
                    isVideo = cameraUiState.isVideoFile,
                    onSend = {
                        cameraVm.onDiscard()
                        onImageCaptured(uri, cameraUiState.isVideoFile)
                    },
                    onDiscard = { cameraVm.onDiscard() }
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (!cameraReady) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { view ->
                            view.scaleType = PreviewView.ScaleType.FILL_CENTER
                            view.setOnTouchListener { _, event ->
                                if (event.action == MotionEvent.ACTION_DOWN) {
                                    focusPoint = Pair(event.x, event.y)
                                    val cam = camera ?: return@setOnTouchListener true
                                    val pv = view as PreviewView
                                    try {
                                        cam.cameraControl.cancelFocusAndMetering()
                                        val factory = pv.meteringPointFactory
                                        val point = factory.createPoint(event.x, event.y)
                                        val action = FocusMeteringAction.Builder(
                                            point, FocusMeteringAction.FLAG_AF
                                        ).build()
                                        cam.cameraControl.startFocusAndMetering(action)
                                    } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;}
                                }
                                true
                            }
                            previewView = view
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (cameraUiState.isRecording) {
                    RecordingTimerBanner(elapsedSeconds = cameraUiState.recordingElapsed)
                }

                CameraControlButtons(
                    cameraVm = cameraVm,
                    uiState = cameraUiState,
                    mainExecutor = mainExecutor,
                    context = context,
                    onImageSavedError = onDismiss,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (!cameraUiState.isRecording) {
                    CameraTipText(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 100.dp)
                    )
                }

                Text(
                    "取消",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 20.dp)
                        .clickable {
                            if (cameraUiState.isRecording) cameraVm.stopRecording()
                            onDismiss()
                        }
                )

                focusPoint?.let { (fx, fy) ->
                    val alphaAnim = remember { Animatable(1f) }
                    val scaleAnim = remember { Animatable(1.4f) }

                    LaunchedEffect(focusPoint) {
                        alphaAnim.snapTo(1f)
                        scaleAnim.snapTo(1.4f)
                        scaleAnim.animateTo(1f, animationSpec = tween(300))
                        kotlinx.coroutines.delay(500)
                        alphaAnim.animateTo(0f, animationSpec = tween(300))
                    }

                    val boxSize = 70.dp
                    val offsetDp = with(density) {
                        (fx - (boxSize.toPx() / 2)).toDp() to (fy - (boxSize.toPx() / 2)).toDp()
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = offsetDp.first, y = offsetDp.second)
                            .size(boxSize * scaleAnim.value)
                            .alpha(alphaAnim.value)
                            .border(1.5.dp, Color(0xFFFFD600))
                    ) {
                        Box(modifier = Modifier.align(Alignment.TopCenter).size(1.dp, 6.dp).background(Color(0xFFFFD600)))
                        Box(modifier = Modifier.align(Alignment.BottomCenter).size(1.dp, 6.dp).background(Color(0xFFFFD600)))
                        Box(modifier = Modifier.align(Alignment.CenterStart).size(6.dp, 1.dp).background(Color(0xFFFFD600)))
                        Box(modifier = Modifier.align(Alignment.CenterEnd).size(6.dp, 1.dp).background(Color(0xFFFFD600)))
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("需要相機權限", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("授予權限")
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("取消", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun CameraTipText(
    modifier: Modifier = Modifier
) {
    Text(
        "點擊以拍照，按住可錄影",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 14.sp,
        modifier = modifier
    )
}

@Composable
private fun RecordingTimerBanner(
    elapsedSeconds: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 48.dp)
                .background(Color(0x80000000), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun enterFullScreen(activity: Activity) {
    val window = activity.window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        @Suppress("DEPRECATION")
        window.setDecorFitsSystemWindows(false)
        window.decorView.windowInsetsController?.let { c ->
            c.hide(android.view.WindowInsets.Type.statusBars())
            c.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}

private fun exitFullScreen(activity: Activity) {
    val window = activity.window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.decorView.windowInsetsController?.let { c ->
            c.show(android.view.WindowInsets.Type.statusBars())
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN.inv() and android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
    }
}

