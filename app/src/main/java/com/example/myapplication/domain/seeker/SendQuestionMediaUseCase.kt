package com.example.myapplication.domain.seeker

import android.net.Uri
import android.util.Log
import com.example.myapplication.data.repository.MediaUploader
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SendMedia(
    val uri: Uri,
    val isVideo: Boolean,
    val isVoice: Boolean
)

class SendQuestionMediaUseCase(
    private val mediaUploader: MediaUploader
) {
    suspend operator fun invoke(
        chatroomId: String,
        messagesRef: DatabaseReference,
        mediaList: List<SendMedia>,
        startTimestamp: Long
    ) = withContext(Dispatchers.IO) {
        mediaList.forEachIndexed { index, media ->
            try {
                val timestamp = startTimestamp + index
                when {
                    media.isVoice -> uploadVoice(chatroomId, messagesRef, media, timestamp)
                    media.isVideo -> uploadVideo(chatroomId, messagesRef, media, timestamp)
                    else -> uploadImages(chatroomId, messagesRef, media, timestamp)
                }
            } catch (e: Exception) {
                Log.w("SendQuestionMedia", "Upload failed", e)
            }
        }
    }

    private suspend fun uploadVoice(chatroomId: String, messagesRef: DatabaseReference, media: SendMedia, timestamp: Long) {
        val voicePath = media.uri.path ?: return
        val voiceUrl = mediaUploader.sendVoice(
            chatroomId = chatroomId,
            filePath = voicePath,
            onProgress = {},
            onError = { Log.w("SendQuestionMedia", "Voice failed: $it") }
        ) ?: return
        val msgId = messagesRef.push().key ?: return
        messagesRef.child(msgId).setValue(mapOf(
            "senderId" to "", "sender" to "user", "text" to "",
            "timestamp" to timestamp, "voiceUrl" to voiceUrl,
            "readBy" to mapOf("system" to true)
        ))
    }

    private suspend fun uploadVideo(chatroomId: String, messagesRef: DatabaseReference, media: SendMedia, timestamp: Long) {
        val videoUrl = mediaUploader.sendVideo(
            chatroomId = chatroomId, uri = media.uri,
            onProgress = {}, onError = { Log.w("SendQuestionMedia", "Video failed: $it") }
        ) ?: return
        val msgId = messagesRef.push().key ?: return
        messagesRef.child(msgId).setValue(mapOf(
            "senderId" to "", "sender" to "user", "text" to "",
            "timestamp" to timestamp, "videoUrl" to videoUrl,
            "readBy" to mapOf("system" to true)
        ))
    }

    private suspend fun uploadImages(chatroomId: String, messagesRef: DatabaseReference, media: SendMedia, timestamp: Long) {
        val urls = mediaUploader.sendImages(
            chatroomId = chatroomId, userId = "", myRole = "user",
            uris = listOf(media.uri),
            onProgress = {}, onError = { Log.w("SendQuestionMedia", "Image failed: $it") }
        )
        if (urls.isEmpty()) return
        val msgId = messagesRef.push().key ?: return
        messagesRef.child(msgId).setValue(mapOf(
            "senderId" to "", "sender" to "user", "text" to "",
            "timestamp" to timestamp, "imageUrls" to urls,
            "readBy" to mapOf("system" to true)
        ))
    }
}
