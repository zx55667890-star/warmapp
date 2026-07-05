package com.example.myapplication.util

import android.graphics.Bitmap

object VideoThumbnailCache {
    private val cache = HashMap<String, Bitmap>()

    fun get(url: String): Bitmap? = cache[url]

    fun put(url: String, bitmap: Bitmap) { cache[url] = bitmap }

    fun clear() { cache.clear() }
}
