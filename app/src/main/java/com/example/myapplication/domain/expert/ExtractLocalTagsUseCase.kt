package com.example.myapplication.domain.expert

import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class ExtractLocalTagsUseCase {

    private data class ModelEntry(val name: String, val rpmHint: Int, val model: GenerativeModel)
    private val models = listOf(
        ModelEntry("gemma-4-31b-it", 15, GenerativeModel("gemma-4-31b-it", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemma-4-26b-a4b-it", 15, GenerativeModel("gemma-4-26b-a4b-it", BuildConfig.GEMINI_API_KEY)),
    )

    private val roundRobin = AtomicInteger(0)
    private val rpmCounters = models.associate { it.name to mutableListOf<Long>() }

    private fun canUseModel(name: String, rpmHint: Int): Boolean {
        val now = System.currentTimeMillis()
        val windowStart = now - 60_000
        synchronized(rpmCounters) {
            val timestamps = rpmCounters[name]!!
            timestamps.removeAll { it < windowStart }
            return timestamps.size < rpmHint
        }
    }

    private fun recordRequest(name: String) {
        synchronized(rpmCounters) {
            rpmCounters[name]!!.add(System.currentTimeMillis())
        }
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
            if (!canUseModel(entry.name, entry.rpmHint)) {
                Log.d("TagExtract", "⏭️ ${entry.name} RPM 已滿，跳過")
                continue
            }
            recordRequest(entry.name)
            try {
                val response = entry.model.generateContent(prompt)
                Log.d("TagExtract", "✅ 使用模型: ${entry.name} (${entry.rpmHint} RPM)")
                return@withContext response.text?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(4) ?: emptyList()
            } catch (e: Exception) {
                Log.w("TagExtract", "❌ 模型 ${entry.name} 失敗: ${e.message}")
            }
        }
        Log.e("TagExtract", "所有模型皆失敗，回傳空標籤")
        emptyList()
    }
}
