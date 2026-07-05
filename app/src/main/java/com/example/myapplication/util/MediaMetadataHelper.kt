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
            } catch (e: Exception) {
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
                // On API 27+ (O_MR1), getFrameAtTime already returns rotated frames
                if (bmp != null && rotation != 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                    val m = Matrix().apply { postRotate(rotation.toFloat()) }
                    val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
                    if (rotated != bmp) bmp.recycle()
                    rotated
                } else bmp
            } catch (e: Exception) {
                null
            }
        }
    }
}
