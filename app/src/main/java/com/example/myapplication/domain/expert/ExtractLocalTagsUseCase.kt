package com.example.myapplication.domain.expert

import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class ExtractLocalTagsUseCase {

    private data class ModelEntry(val name: String, val rpmLimit: Int, val rpdLimit: Int, val model: GenerativeModel)
    private val models = listOf(
        ModelEntry("gemini-3.1-flash-lite", 15, 500, GenerativeModel("gemini-3.1-flash-lite", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-2.5-flash-lite", 10, 20, GenerativeModel("gemini-2.5-flash-lite", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-3.5-flash", 5, 20, GenerativeModel("gemini-3.5-flash", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-2.5-flash", 5, 20, GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-3-flash", 5, 20, GenerativeModel("gemini-3-flash", BuildConfig.GEMINI_API_KEY)),
    )

    private val roundRobin = AtomicInteger(0)
    private val rpmCounters = models.associate { it.name to mutableListOf<Long>() }
    private val rpdCounters = models.associate { it.name to mutableListOf<Long>() }

    private fun canUseModel(name: String, rpmLimit: Int, rpdLimit: Int): Boolean {
        val now = System.currentTimeMillis()
        synchronized(rpmCounters) {
            val timestamps = rpmCounters[name]!!
            timestamps.removeAll { it < now - 60_000 }
            if (timestamps.size >= rpmLimit) return false
        }
        synchronized(rpdCounters) {
            val timestamps = rpdCounters[name]!!
            timestamps.removeAll { it < now - 86_400_000 }
            if (timestamps.size >= rpdLimit) return false
        }
        return true
    }

    private fun recordRequest(name: String) {
        val now = System.currentTimeMillis()
        synchronized(rpmCounters) { rpmCounters[name]!!.add(now) }
        synchronized(rpdCounters) { rpdCounters[name]!!.add(now) }
    }

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.IO) {
        val prompt = """
            請從以下文字中提取 4 個最核心的關鍵字標籤。
            要求：
            1. 只回傳標籤內容，用逗號分隔 (例如：台積電,股票,投資,理財)。
            2. 不要包含任何解釋、序號或符號。
            3. 處理「台積電」、「比特幣」等熱門專有名詞時，請務必完整保留，不要拆開。
            
            文字內容：$text
        """.trimIndent()

        val startIndex = roundRobin.getAndIncrement() % models.size
        for (i in models.indices) {
            val entry = models[(startIndex + i) % models.size]
            if (!canUseModel(entry.name, entry.rpmLimit, entry.rpdLimit)) {
                Log.d("TagExtract", "⏭️ ${entry.name} 限額已滿，跳過")
                continue
            }
            recordRequest(entry.name)
            try {
                val response = entry.model.generateContent(prompt)
                Log.d("TagExtract", "✅ 使用模型: ${entry.name} (RPM ${entry.rpmLimit} / RPD ${entry.rpdLimit})")
                return@withContext response.text?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(4) ?: emptyList()
            } catch (e: Exception) {
                Log.w("TagExtract", "❌ 模型 ${entry.name} 失敗: ${e.message}")
            }
        }
        Log.e("TagExtract", "所有模型皆失敗，回傳空標籤")
        emptyList()
    }
}
