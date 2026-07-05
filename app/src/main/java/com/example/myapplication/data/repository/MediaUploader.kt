package com.example.myapplication.data.repository

import android.net.Uri
import com.example.myapplication.util.ImageUtils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MediaUploader(private val storage: FirebaseStorage) {

    suspend fun sendImages(
        chatroomId: String,
        userId: String,
        myRole: String,
        uris: List<Uri>,
        onProgress: (Float?) -> Unit,
        onError: (String) -> Unit
    ): List<String> {
        if (uris.isEmpty()) return emptyList()

        val context = storage.app.applicationContext

        return withContext(Dispatchers.IO) {
            val deferredList = uris.map { uri ->
                async {
                    try {
                        val mimeType = context.contentResolver.getType(uri)?.takeIf { it.isNotBlank() } ?: run {
                            val name = uri.lastPathSegment?.lowercase() ?: ""
                            when {
                                name.endsWith(".mp4") -> "video/mp4"
                                name.endsWith(".mov") -> "video/quicktime"
                                name.endsWith(".3gp") -> "video/3gpp"
                                name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
                                name.endsWith(".png") -> "image/png"
                                name.endsWith(".gif") -> "image/gif"
                                name.endsWith(".webp") -> "image/webp"
                                else -> ""
                            }
                        }
                        val isVideo = mimeType.startsWith("video/")

                        if (isVideo) {
                            val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.mp4"
                            val ref = storage.reference.child("chat_images/$chatroomId/$fileName")
                            val metadata = StorageMetadata.Builder().setContentType("video/mp4").build()
                            val uploadTask = ref.putFile(uri, metadata)
                            uploadTask.addOnProgressListener { snap ->
                                val total = snap.totalByteCount
                                onProgress(if (total > 0) snap.bytesTransferred.toFloat() / total else 0f)
                            }
                            uploadTask.await()
                            return@async ref.downloadUrl.await().toString()
                        } else {
                            val compressedBytes = ImageUtils.compressAndScaleImage(context, uri)
                            if (compressedBytes == null) {
                                onError("無法讀取圖片，請再試一次")
                                return@async null
                            }

                            val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
                            val ref = storage.reference.child("chat_images/$chatroomId/$fileName")
                            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
                            val uploadTask = ref.putBytes(compressedBytes, metadata)

                            uploadTask.addOnProgressListener { snap ->
                                val total = snap.totalByteCount
                                onProgress(if (total > 0) snap.bytesTransferred.toFloat() / total else 0f)
                            }

                            uploadTask.await()
                            return@async ref.downloadUrl.await().toString()
                        }
                    } catch (e: Exception) {
                        onError(e.message ?: "上傳失敗，請再試一次")
                        return@async null
                    }
                }
            }

            deferredList.mapNotNull { it.await() }
        }
    }

    suspend fun deleteFilesByUrls(urls: List<String>) {
        withContext(Dispatchers.IO) {
            urls.forEach { url ->
                try {
                    if (url.isNotBlank()) {
                        storage.getReferenceFromUrl(url).delete().await()
                    }
                } catch (_: Exception) { }
            }
        }
    }

    suspend fun sendVoice(
        chatroomId: String,
        filePath: String,
        onProgress: (Float?) -> Unit,
        onError: (String) -> Unit
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "voice_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.m4a"
                val ref = storage.reference.child("chat_voice/$chatroomId/$fileName")
                val file = java.io.File(filePath)
                val uploadTask = ref.putFile(android.net.Uri.fromFile(file))
                uploadTask.addOnProgressListener { snap ->
                    val total = snap.totalByteCount
                    onProgress(if (total > 0) snap.bytesTransferred.toFloat() / total else 0f)
                }
                uploadTask.await()
                val downloadUrl = ref.downloadUrl.await().toString()
                downloadUrl
            } catch (e: Exception) {
                onError("語音上傳失敗：${e.message}")
                null
            }
        }
    }
}
