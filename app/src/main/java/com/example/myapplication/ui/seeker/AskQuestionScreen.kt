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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.domain.seeker.SendMedia
import com.example.myapplication.ui.camera.CameraCaptureScreen
import com.example.myapplication.ui.seeker.components.AskQuestionHeader
import com.example.myapplication.ui.seeker.components.AskQuestionInputBar
import com.example.myapplication.ui.seeker.components.AttachmentBottomSheet
import com.example.myapplication.ui.theme.AppColors
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
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onBack)

    LaunchedEffect(userId) { viewModel.refreshQuota(userId) }
    LaunchedEffect(Unit) { viewModel.clearActiveChatRoomId() }

    LaunchedEffect(seekerUiState.quotaError) {
        seekerUiState.quotaError?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
            viewModel.clearQuotaError()
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var wasMatching by remember { mutableStateOf(false) }
    LaunchedEffect(seekerUiState.isUserMatching) {
        if (wasMatching && !seekerUiState.isUserMatching) focusRequester.requestFocus()
        wasMatching = seekerUiState.isUserMatching
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
            selectedMediaList = selectedMediaList + uris.map {
                SelectedMedia(it, isVideo = false, isVoice = false)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppColors.DarkBackground,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBackgroundGlow()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
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
                    val isExhausted = seekerUiState.dailyRemainingQuota == 0
                    Text(
                        text = "今日剩餘提問次數：${seekerUiState.dailyRemainingQuota} 次",
                        color = if (isExhausted)
                            AppColors.StatusError
                        else
                            AppColors.TextGray.copy(alpha = 0.7f),
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

            AnimatedVisibility(
                visible = seekerUiState.isUserMatching &&
                        seekerUiState.activeChatRoomId.isBlank() &&
                        seekerUiState.quotaError == null,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                MatchingOverlay(
                    onCancel = { viewModel.cancelUserMatching() },
                    isPendingAcceptance = seekerUiState.isPendingAcceptance
                )
            }
        }

        if (showCameraCapture) {
            CameraCaptureScreen(
                onImageCaptured = { uri, isVideo ->
                    showCameraCapture = false
                    selectedMediaList = selectedMediaList +
                            SelectedMedia(uri, isVideo, isVoice = false)
                },
                onDismiss = { showCameraCapture = false }
            )
        }

        if (showVoiceRecording) {
            VoiceRecordingScreen(
                onDismiss = { showVoiceRecording = false },
                onVoiceRecorded = { filePath ->
                    showVoiceRecording = false
                    selectedMediaList = selectedMediaList +
                            SelectedMedia(Uri.fromFile(File(filePath)), isVideo = false, isVoice = true)
                }
            )
        }

        if (showAttachSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachSheet = false },
                onGalleryClick = {
                    showAttachSheet = false
                    imagePickerLauncher.launch("image/*")
                },
                onCameraClick = {
                    showAttachSheet = false
                    showCameraCapture = true
                },
                onVoiceClick = {
                    showAttachSheet = false
                    showVoiceRecording = true
                }
            )
        }

    }
}
