package com.example.myapplication.ui.chat

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.myapplication.ui.chat.components.ChatBottomArea
import com.example.myapplication.ui.chat.components.ChatTopBar
import com.example.myapplication.ui.chat.components.MessageList
import com.example.myapplication.ui.chat.components.QuestionBanner
import com.example.myapplication.ui.chat.dialog.ChatDialogHost
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatroomId: String,
    userId: String,
    myRole: String,
    expertId: String,
    expertText: String,
    expertDate: String,
    chatQuestionText: String,
    onBack: () -> Unit
) {
    val viewModel: ChatViewModel = koinViewModel(key = chatroomId)
    LaunchedEffect(Unit) { viewModel.initChat(chatroomId, userId, myRole) }

    val listState = rememberLazyListState()
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCameraCapture by remember { mutableStateOf(false) }
    var showVoiceRecording by remember { mutableStateOf(false) }
    var highlightedMsgId by remember { mutableStateOf<String?>(null) }
    var opponentNickname by remember { mutableStateOf("") }
    var myNickname by remember { mutableStateOf("") }
    val userRepository: UserRepository = koinInject()
    LaunchedEffect(Unit) {
        userRepository.getNickname(userId) { myNickname = it }
        val opponentId = if (myRole == "user") expertId else ""
        if (opponentId.isNotBlank()) {
            userRepository.getNickname(opponentId) { opponentNickname = it }
        }
    }
    LaunchedEffect(uiState.messages) {
        if (myRole != "user" && opponentNickname.isBlank()) {
            val opponentId = uiState.messages.firstOrNull { it.senderId != userId }?.senderId
            if (opponentId != null) userRepository.getNickname(opponentId) { opponentNickname = it }
        }
    }

    ChatScrollManager(
        listState = listState,
        events = viewModel.events,
        messages = uiState.messages,
        pendingMessages = uiState.pendingMessages,
        isInitialLoading = uiState.isInitialLoading,
        isOtherTyping = uiState.isOtherTyping,
        isChatActive = uiState.isChatActive,
        markAllRead = { viewModel.markAllRead() }
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                is ChatEvent.ChatEndedByOther -> {
                    viewModel.onChatEndedByOther(myRole, expertId, onBack)
                }
                is ChatEvent.OpenCamera -> {
                    focusManager.clearFocus()
                    showCameraCapture = true
                }
                is ChatEvent.OpenVoiceRecorder -> {
                    focusManager.clearFocus()
                    showVoiceRecording = true
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(highlightedMsgId) {
        if (highlightedMsgId != null) {
            delay(1500L)
            highlightedMsgId = null
        }
    }

    BackHandler(onBack = {
        if (uiState.isChatActive) {
            viewModel.updateUiState { it.copy(showEndConfirmDialog = true) }
        } else {
            onBack()
        }
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .drawBackgroundGlow()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }

    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                ChatTopBar(
                    myRole = myRole,
                    isChatActive = uiState.isChatActive,
                    isDarkTheme = isDarkTheme,
                    onEndChat = { viewModel.updateUiState { it.copy(showEndConfirmDialog = true) } },
                    onBack = onBack,
                    opponentNickname = opponentNickname,
                    myNickname = myNickname
                )
                QuestionBanner(
                    questionText = chatQuestionText,
                    isDarkTheme = isDarkTheme
                )
            }

            val globalImageUrls by viewModel.globalImageUrls.collectAsStateWithLifecycle()
            val allMessages by remember { derivedStateOf { uiState.messages + uiState.pendingMessages } }
            Box(modifier = Modifier.weight(1f)) {
                MessageList(
                    listState = listState,
                    allMessages = allMessages,
                    confirmedMessagesCount = uiState.messages.size,
                    isLoadingMore = uiState.isLoadingMore,
                    hasMoreMessages = uiState.hasMoreMessages,
                    isInitialLoading = uiState.isInitialLoading,
                    globalImageUrls = globalImageUrls,
                    userId = userId,
                    isDarkTheme = isDarkTheme,
                    isChatActive = uiState.isChatActive,
                    isOtherTyping = uiState.isOtherTyping,
                    highlightedMsgId = highlightedMsgId,
                    onLoadMore = { viewModel.loadMoreMessages() },
                    onRecall = { viewModel.recallMessage(it) },
                    onReply = { msg -> viewModel.updateUiState { it.copy(replyToMessage = msg) } },
                    onImageClick = { urls, idx ->
                        val url = urls.getOrNull(idx)
                        val globalIdx = if (url != null) globalImageUrls.indexOf(url) else -1
                        val chosenUrls = if (globalIdx >= 0) globalImageUrls else urls
                        val cameraFlags = chosenUrls.map { u ->
                            allMessages.any { msg ->
                                msg.isCameraCapture && (msg.imageUrls.contains(u) || msg.localImageUrls.contains(u) || msg.imageUrl == u)
                            }
                        }
                        viewModel.updateUiState {
                            it.copy(
                                fullScreenImageUrls = chosenUrls,
                                fullScreenImageIndex = if (globalIdx >= 0) globalIdx else idx,
                                fullScreenImageIsCameraCapture = cameraFlags
                            )
                        }
                    },
                    onVideoClick = { url -> viewModel.updateUiState { s -> s.copy(videoUrl = url) } },
                    onAvatarClick = { msg ->
                        if (msg.senderId != userId) {
                            val targetId = if (myRole == "user") expertId else msg.senderId
                            viewModel.fetchOpponentProfile(targetId)
                        }
                    },
                    onQuoteClick = { highlightedMsgId = it }
                )
            }

            ChatBottomArea(
                uiState = uiState,
                isDarkTheme = isDarkTheme,
                onSendMessage = { text -> viewModel.sendMessage(text) },
                onSendImage = { uris -> viewModel.mediaSender.sendImages(chatroomId, userId, myRole, uris) },
                onTypingStatusChange = { viewModel.updateTypingStatus(it) },
                onCameraClick = { viewModel.openCamera() },
                onMicClick = { viewModel.openVoiceRecorder() },
                onDismissReply = { viewModel.updateUiState { it.copy(replyToMessage = null) } }
            )
        }

    ChatDialogHost(
        uiState = uiState,
        viewModel = viewModel,
        myRole = myRole,
        userId = userId,
        chatroomId = chatroomId,
        expertId = expertId,
        expertText = expertText,
        expertDate = expertDate,
        showCameraCapture = showCameraCapture,
        showVoiceRecording = showVoiceRecording,
        onCameraDismiss = { showCameraCapture = false },
        onVoiceDismiss = { showVoiceRecording = false },
        onBack = onBack
    )
}
