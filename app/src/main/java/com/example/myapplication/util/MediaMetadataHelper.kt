package com.example.myapplication.util

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaMetadataHelper {

    suspend fun getDuration(url: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(url)
                val result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                result?.toLongOrNull() ?: 0L
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                0L
            }
        }
    }

    suspend fun getVideoFrame(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                if (url.startsWith("http")) {
                    retriever.setDataSource(url, java.util.HashMap<String, String>().apply { put("User-Agent", "Mozilla/5.0") })
                } else {
                    retriever.setDataSource(url)
                }
                val bmp = retriever.getFrameAtTime(0)
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                retriever.release()
                bmp
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                null
            }
        }
    }
}

