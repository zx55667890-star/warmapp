package com.example.myapplication.ui.seeker

import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.di.SeekerViewModel
import com.example.myapplication.domain.seeker.SendMedia
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
    val seekerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

    // 🛡️ 新增：進入畫面時，自動獲取並重整最新提問額度
    LaunchedEffect(userId) {
        viewModel.refreshQuota(userId)
    }

    // 🛡️ 新增：攔截並監聽 ViewModel 拋出的額度超標/多開限制警告
    LaunchedEffect(seekerUiState.quotaError) {
        seekerUiState.quotaError?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.clearQuotaError() // 顯示完畢後立刻洗掉狀態
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val onSendQuestion = {
        if (selectedMediaList.isNotEmpty() || question.isNotBlank()) {
            focusManager.clearFocus()
            val media = selectedMediaList.map { SendMedia(it.uri, it.isVideo, it.isVoice) }
            viewModel.sendQuestion(question, userId, media)
            question = ""
            selectedMediaList = emptyList()
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

            val imeInsets = WindowInsets.ime
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -imeInsets.getBottom(density).toFloat()
                    }
                    .drawBackgroundGlow()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                AskQuestionHeader(
                    nickname = nickname,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "今日剩餘提問次數：${seekerUiState.dailyRemainingQuota} 次",
                        color = if (seekerUiState.dailyRemainingQuota == 0) Color(0xFFEF5350) else Color.Gray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                AskQuestionInputBar(
                    question = question,
                    onQuestionChange = { question = it },
                    selectedMediaList = selectedMediaList,
                    focusRequester = focusRequester,
                    showSentFeedback = seekerUiState.isUserMatching,
                    onAttachClick = {
                        focusManager.clearFocus()
                        showAttachSheet = true
                    },
                    onSendClick = onSendQuestion,
                    onRemoveMedia = { media -> selectedMediaList = selectedMediaList - media }
                )
            }

            // 全螢幕配對中覆蓋層
            AnimatedVisibility(
                visible = seekerUiState.isUserMatching && seekerUiState.activeChatRoomId.isBlank() && seekerUiState.quotaError == null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                MatchingOverlay(
                    onCancel = { viewModel.cancelUserMatching() }
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
