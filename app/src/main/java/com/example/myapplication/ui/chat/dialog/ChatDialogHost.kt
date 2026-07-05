package com.example.myapplication.ui.chat.dialog

import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.ui.chat.ChatUiState
import com.example.myapplication.ui.chat.ChatViewModel
import com.example.myapplication.ui.camera.CameraCaptureScreen
import com.example.myapplication.ui.components.FullScreenImageDialog
import com.example.myapplication.ui.components.RatingDialog
import com.example.myapplication.ui.components.VideoPlayerDialog
import com.example.myapplication.ui.voice.VoiceRecordingScreen
import org.koin.compose.koinInject

@UnstableApi
@Composable
fun ChatDialogHost(
    uiState: ChatUiState,
    viewModel: ChatViewModel,
    myRole: String,
    userId: String,
    chatroomId: String,
    expertId: String,
    expertText: String,
    expertDate: String,
    showCameraCapture: Boolean,
    showVoiceRecording: Boolean,
    onCameraDismiss: () -> Unit,
    onVoiceDismiss: () -> Unit,
    onBack: () -> Unit,
) {
    val questionRepository: QuestionRepository = koinInject()

    if (uiState.showEndConfirmDialog) {
        EndChatConfirmDialog(
            onConfirm = {
                viewModel.updateUiState { it.copy(showEndConfirmDialog = false) }
                viewModel.markChatEnded(
                    onSuccess = {
                        if (myRole == "user" && userId != expertId) {
                            viewModel.updateUiState { it.copy(showRatingDialog = true) }
                        } else onBack()
                    },
                    onError = { errMsg -> viewModel.updateUiState { it.copy(endChatError = errMsg) } }
                )
            },
            onDismiss = { viewModel.updateUiState { it.copy(showEndConfirmDialog = false) } }
        )
    }

    if (uiState.showRatingDialog) {
        RatingDialog(
            ratingScore = uiState.ratingScore,
            ratingComment = uiState.ratingComment,
            ratingError = uiState.ratingError,
            ratingSubmitting = uiState.ratingSubmitting,
            onScoreChange = { score -> viewModel.updateUiState { it.copy(ratingScore = score) } },
            onCommentChange = { comment -> viewModel.updateUiState { it.copy(ratingComment = comment) } },
            onSubmit = {
                if (uiState.ratingScore == 0) {
                    viewModel.updateUiState { it.copy(ratingError = "請先選擇評分") }
                } else {
                    viewModel.updateUiState { it.copy(ratingSubmitting = true, ratingError = null) }
                    questionRepository.submitRating(
                        expertId = expertId,
                        score = uiState.ratingScore.toDouble(),
                        comment = uiState.ratingComment.trim(),
                        activeChatRoomId = chatroomId,
                        callback = object : QuestionRepository.RatingCallback {
                            override fun onSuccess() {
                                viewModel.updateUiState { it.copy(ratingSubmitting = false, showRatingDialog = false) }
                                onBack()
                            }
                            override fun onError(message: String) {
                                viewModel.updateUiState { it.copy(ratingSubmitting = false, ratingError = message) }
                            }
                        }
                    )
                }
            },
            onSkip = { viewModel.updateUiState { it.copy(showRatingDialog = false) }; onBack() },
            onDismiss = { viewModel.updateUiState { it.copy(showRatingDialog = false) }; onBack() }
        )
    }

    uiState.fullScreenImageUrls?.let { urls ->
        FullScreenImageDialog(
            imageUrls = urls,
            startIndex = uiState.fullScreenImageIndex,
            isCameraCaptureList = uiState.fullScreenImageIsCameraCapture,
            onDismiss = { viewModel.updateUiState { it.copy(fullScreenImageUrls = null) } }
        )
    }

    uiState.videoUrl?.let { url ->
        VideoPlayerDialog(videoUrl = url, onDismiss = { viewModel.updateUiState { s -> s.copy(videoUrl = null) } })
    }

    if (uiState.showOpponentProfile) {
        val opId = if (myRole == "user") expertId else uiState.messages.firstOrNull { it.senderId != userId }?.senderId ?: ""
        val opRole = if (myRole == "user") "expert" else "user"
        OpponentProfileDialog(
            opponentId = opId,
            role = opRole,
            nickname = uiState.opponentNickname.ifEmpty { if (opRole == "expert") "專家" else "提問者" },
            rating = uiState.opponentRating,
            helpCount = uiState.opponentHelpCount,
            experienceText = if (opRole == "expert") expertText else null,
            experienceDate = if (opRole == "expert") expertDate else null,
            onDismiss = { viewModel.updateUiState { it.copy(showOpponentProfile = false) } }
        )
    }

    if (showCameraCapture) {
        CameraCaptureScreen(
            onImageCaptured = { uri, isVideo ->
                onCameraDismiss()
                if (isVideo) {
                    viewModel.mediaSender.sendVideo(chatroomId, userId, myRole, uri, isCameraCapture = true)
                } else {
                    viewModel.mediaSender.sendImages(chatroomId, userId, myRole, listOf(uri), isCameraCapture = true)
                }
            },
            onDismiss = onCameraDismiss
        )
    }

    if (showVoiceRecording) {
        VoiceRecordingScreen(
            onDismiss = onVoiceDismiss,
            onVoiceRecorded = { filePath ->
                onVoiceDismiss()
                viewModel.mediaSender.sendVoice(chatroomId, userId, myRole, filePath)
            }
        )
    }
}
