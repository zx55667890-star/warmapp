package com.example.myapplication.domain.expert

import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

class ExtractLocalTagsUseCase(
    private val sharedPrefs: SharedPreferences,
    private val firebaseDb: FirebaseDatabase
) {

    private data class ModelEntry(val name: String, val rpmLimit: Int, val rpdLimit: Int, val model: GenerativeModel)

    private val models = listOf(
        ModelEntry("gemini-3.1-flash-lite", 15, 500, GenerativeModel("gemini-3.1-flash-lite", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-2.5-flash-lite", 10, 20, GenerativeModel("gemini-2.5-flash-lite", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-3.5-flash", 5, 20, GenerativeModel("gemini-3.5-flash", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-2.5-flash", 5, 20, GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY)),
        ModelEntry("gemini-3-flash-preview", 5, 20, GenerativeModel("gemini-3-flash-preview", BuildConfig.GEMINI_API_KEY)),
    )

    private val roundRobin = AtomicInteger(0)
    private val counterLock = Any()
    private val rpmCounters = models.associate { it.name to mutableListOf<Long>() }

    private var serverOffsetMs: Long? = null

    private fun nowMs(): Long = System.currentTimeMillis() + (serverOffsetMs ?: 0L)

    private fun todayStartMs(): Long {
        val now = Instant.ofEpochMilli(nowMs())
        return now.atZone(ZoneId.of("America/Los_Angeles")).toLocalDate().atStartOfDay(ZoneId.of("America/Los_Angeles")).toInstant().toEpochMilli()
    }

    private val rpdCounters = models.associate { entry ->
        val saved = sharedPrefs.getStringSet("rpd_${entry.name}", emptySet()) ?: emptySet()
        entry.name to saved.mapNotNull { it.toLongOrNull() }.toMutableList()
    }

    private suspend fun ensureOffset() {
        if (serverOffsetMs != null) return
        val deferred = CompletableDeferred<Long>()
        firebaseDb.reference.child(".info/serverTimeOffset")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    deferred.complete(snapshot.getValue(Long::class.java) ?: 0L)
                }
                override fun onCancelled(error: DatabaseError) {
                    deferred.complete(0L)
                }
            })
        serverOffsetMs = deferred.await()
        Log.d("TagExtract", "🕐 伺服器時間偏移: ${serverOffsetMs}ms")
    }

    private fun canUseModel(name: String, rpmLimit: Int, rpdLimit: Int): Boolean {
        val now = nowMs()
        val dayStart = todayStartMs()

        val bannedUntil = sharedPrefs.getLong("quota_banned_$name", 0L)
        if (bannedUntil > now) {
            Log.d("TagExtract", "⏭️ $name 伺服器配額已滿，封鎖至 ${Instant.ofEpochMilli(bannedUntil).atZone(ZoneId.of("America/Los_Angeles"))}")
            return false
        }

        synchronized(counterLock) {
            val rpmList = rpmCounters[name]!!
            rpmList.removeAll { it < now - 60_000 }
            if (rpmList.size >= rpmLimit) return false

            val rpdList = rpdCounters[name]!!
            rpdList.removeAll { it < dayStart }
            if (rpdList.size >= rpdLimit) {
                Log.d("TagExtract", "⏭️ $name RPD 已滿 (${rpdList.size}/$rpdLimit)")
                return false
            }
        }
        return true
    }

    private fun recordRequest(name: String) {
        val now = nowMs()
        val dayStart = todayStartMs()
        synchronized(counterLock) {
            rpmCounters[name]!!.add(now)
            rpdCounters[name]!!.add(now)
            val rpdList = rpdCounters[name]!!
            rpdList.removeAll { it < dayStart }
            sharedPrefs.edit()
                .putStringSet("rpd_$name", rpdList.map { it.toString() }.toSet())
                .apply()
        }
    }

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.IO) {
        ensureOffset()

        val prompt = """
            請從以下文字中提取 4 個最核心的關鍵字標籤。
            要求：
            1. 只回傳標籤內容，用逗號分隔 (例如：台積電,股票,投資,理財)。
            2. 不要包含任何解釋、序號或符號。
            3. 處理「台積電」、「比特幣」等熱門專有名詞時，請務必完整保留，不要拆開。
            
            文字內容：$text
        """.trimIndent()

        val rawIndex = roundRobin.getAndIncrement()
        val startIndex = (rawIndex and Int.MAX_VALUE) % models.size

        for (i in models.indices) {
            val entry = models[(startIndex + i) % models.size]
            if (!canUseModel(entry.name, entry.rpmLimit, entry.rpdLimit)) {
                continue
            }

            recordRequest(entry.name)
            try {
                val response = entry.model.generateContent(prompt)
                Log.d("TagExtract", "✅ 使用模型: ${entry.name} (RPM ${entry.rpmLimit} / RPD ${entry.rpdLimit})")
                return@withContext response.text?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(4) ?: emptyList()
            } catch (e: Exception) {
                Log.w("TagExtract", "❌ 模型 ${entry.name} 失敗: ${e.message}")
                if (e.message?.contains("Quota exceeded", ignoreCase = true) == true) {
                    val banUntil = nowMs() + 86_400_000
                    sharedPrefs.edit().putLong("quota_banned_${entry.name}", banUntil).apply()
                    Log.d("TagExtract", "🚫 ${entry.name} 已封鎖 24 小時")
                }
            }
        }
        Log.e("TagExtract", "所有模型皆失敗，回傳空標籤")
        emptyList()
    }
}
