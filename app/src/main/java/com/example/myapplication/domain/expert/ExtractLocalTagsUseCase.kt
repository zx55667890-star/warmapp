package com.example.myapplication.domain.expert

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.myapplication.BuildConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.ThinkingConfig
import com.google.genai.types.ThinkingLevel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger

class ExtractLocalTagsUseCase(
    private val sharedPrefs: SharedPreferences,
    private val firebaseDb: FirebaseDatabase
) {

    private data class ModelEntry(val name: String, val rpmLimit: Int, val rpdLimit: Int, val supportsThinking: Boolean)

    private val models = listOf(
        ModelEntry("gemini-3.1-flash-lite", 15, 500, false),
        ModelEntry("gemini-2.5-flash-lite", 10, 20, false),
        ModelEntry("gemini-3.5-flash", 5, 20, true),
        ModelEntry("gemini-3-flash-preview", 5, 20, true),
        ModelEntry("gemini-2.5-flash", 5, 20, true),
    )

    private val client = Client.builder().apiKey(BuildConfig.GEMINI_API_KEY).build()
    private val emptyConfig = GenerateContentConfig.builder().build()

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
        // 讀取時移除可能存在的隨機後綴
        entry.name to saved.mapNotNull { it.substringBefore('_').toLongOrNull() }.toMutableList()
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
        serverOffsetMs = withTimeoutOrNull(3000) { deferred.await() } ?: 0L
        Log.d("TagExtract", "serverTimeOffset: ${serverOffsetMs}ms")
    }

    private fun canUseModel(name: String, rpmLimit: Int, rpdLimit: Int): Boolean {
        val now = nowMs()
        val dayStart = todayStartMs()

        val bannedUntil = sharedPrefs.getLong("quota_banned_$name", 0L)
        if (bannedUntil > now) {
            Log.d("TagExtract", "$name quota banned until ${Instant.ofEpochMilli(bannedUntil).atZone(ZoneId.of("America/Los_Angeles"))}")
            return false
        }

        synchronized(counterLock) {
            val rpmList = rpmCounters[name]!!
            rpmList.removeAll { it < now - 60_000 }
            if (rpmList.size >= rpmLimit) return false

            val rpdList = rpdCounters[name]!!
            rpdList.removeAll { it < dayStart }

            Log.d("TagExtract", "Check $name -> RPD: ${rpdList.size}/$rpdLimit, RPM: ${rpmList.size}/$rpmLimit")

            if (rpdList.size >= rpdLimit) {
                Log.d("TagExtract", "$name RPD full (${rpdList.size}/$rpdLimit)")
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
            // 在時間戳後加上隨機後綴，防止 StringSet 過濾掉同毫秒的請求
            val toSave = rpdList.mapIndexed { index, ts -> "${ts}_$index" }.toSet()
            sharedPrefs.edit { putStringSet("rpd_$name", toSave) }
        }
    }

    private fun migrateOldBans() {
        val nextMidnight = todayStartMs() + 86_400_000
        models.forEach { entry ->
            val key = "quota_banned_${entry.name}"
            val existing = sharedPrefs.getLong(key, 0L)
            if (existing > nextMidnight) {
                sharedPrefs.edit { putLong(key, nextMidnight) }
                Log.d("TagExtract", "${entry.name} ban corrected to Pacific midnight")
            }
        }
    }

    private fun buildConfig(entry: ModelEntry): GenerateContentConfig? {
        if (!entry.supportsThinking) return null
        val thinkingConfig = if (entry.name.contains("3.")) {
            ThinkingConfig.builder()
                .thinkingLevel(ThinkingLevel("off"))
                .build()
        } else {
            ThinkingConfig.builder()
                .thinkingBudget(0)
                .build()
        }
        return GenerateContentConfig.builder()
            .thinkingConfig(thinkingConfig)
            .build()
    }

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.IO) {
        ensureOffset()
        migrateOldBans()

        val prompt = """
            請從以下文字中提取 4 個最核心的關鍵字標籤。

            警告：
            如果輸入的文字是無意義的胡言亂語、鍵盤亂打、重複的符號、生僻字亂堆、或是完全無法對應到任何實際生活、工作或專業技能場景的内容，
            你必須「立刻拒絕生成」，且只回傳一個空字串，不要帶有任何標籤、字元、標點符號或解釋。

            一般提取要求：
            1. 只回傳標籤內容，用逗號分隔 (例如：台積電,股票,投資,理財)。
            2. 不要包含任何解釋、序號或符號。
            3. 處理「台積電」、「比特幣」等熱門專有名詞時，請務必完整保留，不要分開。

            文字內容：$text
        """.trimIndent()

        val hasAnyAvailableModel = models.any { canUseModel(it.name, it.rpmLimit, it.rpdLimit) }
        if (!hasAnyAvailableModel) {
            Log.w("TagExtract", "all models quota full, cannot request")
            throw IllegalStateException("AI service quota full, please try later or enter tags manually")
        }

        val rawIndex = roundRobin.getAndIncrement()
        val startIndex = (rawIndex and Int.MAX_VALUE) % models.size

        for (i in models.indices) {
            val entry = models[(startIndex + i) % models.size]
            if (!canUseModel(entry.name, entry.rpmLimit, entry.rpdLimit)) {
                continue
            }

            recordRequest(entry.name)
            try {
                val config = if (entry.supportsThinking) buildConfig(entry)!! else emptyConfig
                val response = client.models.generateContent(entry.name, prompt, config)
                val responseText = response.text()
                
                // 如果模型回傳 null 或空字串，代表它遵循指令「拒絕為胡言亂語生成標籤」
                // 這屬於「成功識別」而非「執行失敗」，應直接結束並回傳空列表，不需輪詢下一個模型
                if (responseText.isNullOrBlank()) {
                    Log.d("TagExtract", "Model ${entry.name} identified gibberish or returned empty. Stopping.")
                    return@withContext emptyList<String>()
                }

                val currentRpd = rpdCounters[entry.name]?.size ?: 0
                Log.d("TagExtract", "OK [${entry.name}] - Today's count: $currentRpd/${entry.rpdLimit}")
                return@withContext responseText.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }.take(4)
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                Log.w("TagExtract", "model ${entry.name} failed: ${e.message}")
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("Quota exceeded", ignoreCase = true) || errorMsg.contains("429")) {
                    val banUntil = todayStartMs() + 86_400_000
                    sharedPrefs.edit { putLong("quota_banned_${entry.name}", banUntil) }
                    Log.d("TagExtract", "${entry.name} banned until Pacific midnight")
                }
            }
        }
        Log.e("TagExtract", "all models exhausted after retry")
        throw IllegalStateException("AI tag extraction failed, please enter tags manually")
    }
}

