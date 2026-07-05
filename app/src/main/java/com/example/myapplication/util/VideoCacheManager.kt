package com.example.myapplication.util

import android.content.Context
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.common.util.UnstableApi
import java.io.File

@UnstableApi
object VideoCacheManager {
    private var videoCache: SimpleCache? = null
    private var databaseProvider: DatabaseProvider? = null

    fun getCache(context: Context): SimpleCache {
        if (videoCache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            if (databaseProvider == null) {
                databaseProvider = StandaloneDatabaseProvider(context)
            }
            videoCache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(50L * 1024 * 1024), databaseProvider!!)
        }
        return videoCache!!
    }

    fun release() {
        videoCache?.release()
        videoCache = null
    }
}
