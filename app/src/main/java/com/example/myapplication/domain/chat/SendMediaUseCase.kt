package com.example.myapplication.domain.chat

import android.net.Uri
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.MessageRepositoryFactory
import com.example.myapplication.util.MediaMetadataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SendMediaUseCase(
    private val repoFactory: MessageRepositoryFactory,
    private val mediaUploader: MediaUploader
) {
    suspend fun sendImages(
        chatroomId: String,
        userId: String,
        myRole: String,
        uris: List<Uri>,
        onProgress: (Float?) -> Unit,
        onError: (String) -> Unit
    ): List<String> {
        if (uris.isEmpty()) return emptyList()
        val messageRepo = repoFactory.create(chatroomId)
        val downloadUrls = mediaUploader.sendImages(
            chatroomId = chatroomId,
            userId = userId,
            myRole = myRole,
            uris = uris,
            onProgress = onProgress,
            onError = onError
        )
        if (downloadUrls.isNotEmpty()) {
            messageRepo.sendMessageWithFields(
                userId, myRole,
                mapOf(
                    "text" to "",
                    "imageUrl" to downloadUrls.first(),
                    "imageUrls" to downloadUrls,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
        return downloadUrls
    }

    suspend fun sendVideo(
        chatroomId: String,
        userId: String,
        myRole: String,
        uri: Uri,
        onProgress: (Float?) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        val messageRepo = repoFactory.create(chatroomId)
        val downloadUrl = mediaUploader.sendVideo(
            chatroomId = chatroomId,
            uri = uri,
            onProgress = onProgress,
            onError = onError
        )
        if (downloadUrl != null) {
            messageRepo.sendMessageWithFields(
                userId, myRole,
                mapOf(
                    "text" to "",
                    "videoUrl" to downloadUrl,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            return true
        }
        return false
    }

    suspend fun sendVoice(
        chatroomId: String,
        userId: String,
        myRole: String,
        filePath: String,
        onProgress: (Float?) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        val messageRepo = repoFactory.create(chatroomId)
        val downloadUrl = mediaUploader.sendVoice(
            chatroomId = chatroomId,
            filePath = filePath,
            onProgress = onProgress,
            onError = onError
        )
        return if (downloadUrl != null) {
            val duration = MediaMetadataHelper.getDuration(filePath)
            messageRepo.sendMessageWithFields(
                userId, myRole,
                mapOf(
                    "text" to "",
                    "voiceUrl" to downloadUrl,
                    "voiceDuration" to duration,
                    "timestamp" to System.currentTimeMillis()
                )
            )
            true
        } else false
    }

    suspend fun deleteMessageMedia(urls: List<String>) {
        mediaUploader.deleteFilesByUrls(urls)
    }

    fun createPendingMessage(
        userId: String,
        myRole: String,
        uri: Uri,
        isVideo: Boolean = false,
        isCameraCapture: Boolean = false
    ): ChatMessage {
        val id = "pending_${System.currentTimeMillis()}"
        return if (isVideo) {
            ChatMessage(
                id = id, senderId = userId, senderRole = myRole,
                text = "", imageUrl = "", videoUrl = uri.toString(),
                timestamp = System.currentTimeMillis(),
                isCameraCapture = isCameraCapture
            )
        } else {
            ChatMessage(
                id = id, senderId = userId, senderRole = myRole,
                text = "", imageUrl = "", imageUrls = listOf(uri.toString()),
                timestamp = System.currentTimeMillis(),
                isCameraCapture = isCameraCapture
            )
        }
    }
}
