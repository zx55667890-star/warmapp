package com.example.myapplication.ui.seeker

import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.di.SeekerViewModel
import com.example.myapplication.ui.camera.CameraCaptureScreen
import com.example.myapplication.ui.seeker.components.AskQuestionHeader
import com.example.myapplication.ui.seeker.components.AskQuestionInputBar
import com.example.myapplication.ui.seeker.components.AttachmentBottomSheet
import com.example.myapplication.ui.voice.VoiceRecordingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskQuestionScreen(
    viewModel: SeekerViewModel,
    userId: String,
    nickname: String = "",
    onBack: () -> Unit = {}
) {
    var question by remember { mutableStateOf("") }
    var selectedMediaList by remember { mutableStateOf<List<SelectedMedia>>(emptyList()) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }
    var showVoiceRecording by remember { mutableStateOf(false) }
    var showSentFeedback by remember { mutableStateOf(false) }

    val seekerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val onSendQuestion = {
        if (selectedMediaList.isNotEmpty() || question.isNotBlank()) {
            val media = selectedMediaList.map { SeekerViewModel.SendMedia(it.uri, it.isVideo, it.isVoice) }
            viewModel.sendQuestion(question, userId, media)
            question = ""
            selectedMediaList = emptyList()
            showSentFeedback = true
        }
    }

    LaunchedEffect(showSentFeedback) {
        if (showSentFeedback) {
            snackbarHostState.showSnackbar("問題已送出，正在為您配對專家")
            delay(1500)
            showSentFeedback = false
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedMediaList = selectedMediaList + uris.map { SelectedMedia(it, isVideo = false, isVoice = false) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {

            // 🌌 1. 獨立的背景層：永遠保持滿版高度，只做「整體的上下平移」
            val imeInsets = WindowInsets.ime
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 讀取鍵盤高度，並將整個畫布往上推 (負值)
                        translationY = -imeInsets.getBottom(density).toFloat()
                    }
                    .drawBackgroundGlow()
            )

            // 📝 2. 內容層：原本的對話佈局，由 safeDrawing 幫你完美處理鍵盤推擠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                AskQuestionHeader(
                    nickname = nickname,
                    modifier = Modifier.weight(1f) // 只要 Column 有 fillMaxSize()，這裡就會乖乖把輸入框往下推
                )
            AskQuestionInputBar(
                question = question,
                onQuestionChange = { question = it },
                selectedMediaList = selectedMediaList,
                focusRequester = focusRequester,
                showSentFeedback = showSentFeedback,
                onAttachClick = {
                    focusManager.clearFocus()
                    showAttachSheet = true
                },
                onSendClick = onSendQuestion,
                onRemoveMedia = { media -> selectedMediaList = selectedMediaList - media }
            )
        }
    }

    if (showCameraCapture) {
        CameraCaptureScreen(
            onImageCaptured = { uri, isVideo ->
                showCameraCapture = false
                selectedMediaList = selectedMediaList + SelectedMedia(uri, isVideo, isVoice = false)
            },
            onDismiss = { showCameraCapture = false },
            preWarmFuture = cameraProviderFuture
        )
    }

    if (showVoiceRecording) {
        VoiceRecordingScreen(
            onDismiss = { showVoiceRecording = false },
            onVoiceRecorded = { filePath ->
                showVoiceRecording = false
                selectedMediaList = selectedMediaList + SelectedMedia(Uri.fromFile(File(filePath)), isVideo = false, isVoice = true)
            }
        )
    }

    if (showAttachSheet) {
        AttachmentBottomSheet(
            onDismiss = { showAttachSheet = false },
            onGalleryClick = { showAttachSheet = false; imagePickerLauncher.launch("image/*") },
            onCameraClick = { showAttachSheet = false; showCameraCapture = true },
            onVoiceClick = { showAttachSheet = false; showVoiceRecording = true }
        )
    }

    if (seekerUiState.showSeekerConfirmDialog) {
        SeekerConfirmDialog(
            expertName = seekerUiState.matchedExpertId,
            expertText = seekerUiState.matchedExpertText,
            expertDate = seekerUiState.matchedExpertDate,
            onConfirm = { viewModel.acceptExpertMatch() },
            onDismiss = { viewModel.rejectExpertMatch() }
        )
    }
}
}
