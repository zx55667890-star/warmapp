package com.example.myapplication.ui.chat

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.domain.chat.SendMediaUseCase

class ChatMediaSender(
    private val sendMediaUseCase: SendMediaUseCase,
    private val scope: CoroutineScope
) {
    private val pendingUploadJobs = mutableMapOf<String, Job>()
    private val cancelledPendingIds = mutableSetOf<String>()
    var onPendingAdded: ((ChatMessage) -> Unit)? = null
    var onPendingRemoved: ((String) -> Unit)? = null
    var onMessageAdded: ((ChatMessage) -> Unit)? = null
    var onScrollToBottom: (() -> Unit)? = null
    var onShowSnackbar: ((String) -> Unit)? = null

    private fun sendMedia(
        pendingMsg: ChatMessage,
        upload: suspend () -> Boolean
    ) {
        onPendingAdded?.invoke(pendingMsg)

        val id = pendingMsg.id
        val job = scope.launch {
            val success = try {
                upload()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onPendingRemoved?.invoke(id)
                    onShowSnackbar?.invoke(e.message ?: "上傳失敗")
                }
                return@launch
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    pendingUploadJobs.remove(id)
                    val realMsg = pendingMsg.copy(id = "uploaded_$id")
                    onMessageAdded?.invoke(realMsg)
                } else {
                    onPendingRemoved?.invoke(id)
                }
            }
        }
        pendingUploadJobs[id] = job
    }

    fun sendVideo(chatroomId: String, userId: String, myRole: String, uri: Uri, isCameraCapture: Boolean = false, onError: (String) -> Unit = {}) {
        val basePendingMsg = sendMediaUseCase.createPendingMessage(userId, myRole, uri, isVideo = true, isCameraCapture = isCameraCapture)
        val pendingMsg = basePendingMsg.copy(localId = basePendingMsg.id)
        var lastError: String? = null
        sendMedia(pendingMsg) {
            lastError = null
            val ok = sendMediaUseCase.sendVideo(chatroomId, userId, myRole, uri, onProgress = {}) { err ->
                lastError = err; onError(err)
            }
            if (!ok) onShowSnackbar?.invoke(lastError ?: "影片上傳失敗")
            ok
        }
    }

    fun sendImages(chatroomId: String, userId: String, myRole: String, uris: List<Uri>, isCameraCapture: Boolean = false, onError: (String) -> Unit = {}) {
        if (uris.isEmpty()) return
        val basePendingMsg = sendMediaUseCase.createPendingMessage(userId, myRole, uris.first(), isCameraCapture = isCameraCapture)
        val pendingMsg = if (uris.size > 1) {
            basePendingMsg.copy(
                imageUrl = "", 
                imageUrls = uris.map { it.toString() },
                localId = basePendingMsg.id,
                localImageUrls = uris.map { it.toString() }
            )
        } else {
            basePendingMsg.copy(
                localId = basePendingMsg.id,
                localImageUrls = listOf(uris.first().toString())
            )
        }
        var lastError: String? = null
        sendMedia(pendingMsg) {
            lastError = null
            val urls = sendMediaUseCase.sendImages(chatroomId, userId, myRole, uris, onProgress = {}) { err ->
                lastError = err; onError(err)
            }
            if (urls.isEmpty()) onShowSnackbar?.invoke(lastError ?: "圖片上傳失敗")
            urls.isNotEmpty()
        }
    }

    fun sendVoice(chatroomId: String, userId: String, myRole: String, filePath: String) {
        val id = "pending_voice_${System.currentTimeMillis()}"
        val pendingMsg = ChatMessage(
            id = id, senderId = userId, senderRole = myRole,
            text = "", voiceUrl = filePath, timestamp = System.currentTimeMillis()
        )
        sendMedia(pendingMsg) {
            sendMediaUseCase.sendVoice(chatroomId, userId, myRole, filePath, onProgress = {}) { err ->
                onShowSnackbar?.invoke(err)
            }
        }
    }

    fun recallMessage(msg: ChatMessage, isPending: Boolean) {
        if (isPending) {
            cancelledPendingIds.add(msg.id)
            pendingUploadJobs[msg.id]?.cancel()
            pendingUploadJobs.remove(msg.id)
            onPendingRemoved?.invoke(msg.id)
        } else {
            val mediaUrls = listOfNotNull(
                msg.imageUrl.takeIf { it.isNotBlank() },
                msg.videoUrl.takeIf { it.isNotBlank() },
                msg.voiceUrl.takeIf { it.isNotBlank() }
            ) + msg.imageUrls.filter { it.isNotBlank() }
            if (mediaUrls.isNotEmpty()) {
                scope.launch {
                    sendMediaUseCase.deleteMessageMedia(mediaUrls)
                }
            }
        }
    }

    fun cancelPending(msgId: String) {
        cancelledPendingIds.add(msgId)
        pendingUploadJobs[msgId]?.cancel()
        pendingUploadJobs.remove(msgId)
    }
}
