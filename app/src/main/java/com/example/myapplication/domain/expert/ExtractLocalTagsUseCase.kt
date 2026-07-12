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
        serverOffsetMs = withTimeoutOrNull(3000) { deferred.await() } ?: 0L
        Log.d("TagExtract", "ðŸ• ä¼ºæœå™¨æ™‚é–“åç§»: ${serverOffsetMs}ms")
    }

    private fun canUseModel(name: String, rpmLimit: Int, rpdLimit: Int): Boolean {
        val now = nowMs()
        val dayStart = todayStartMs()

        val bannedUntil = sharedPrefs.getLong("quota_banned_$name", 0L)
        if (bannedUntil > now) {
            Log.d("TagExtract", "â­ï¸ $name ä¼ºæœå™¨é…é¡å·²æ»¿ï¼Œå°éŽ–è‡³ ${Instant.ofEpochMilli(bannedUntil).atZone(ZoneId.of("America/Los_Angeles"))}")
            return false
        }

        synchronized(counterLock) {
            val rpmList = rpmCounters[name]!!
            rpmList.removeAll { it < now - 60_000 }
            if (rpmList.size >= rpmLimit) return false

            val rpdList = rpdCounters[name]!!
            rpdList.removeAll { it < dayStart }
            if (rpdList.size >= rpdLimit) {
                Log.d("TagExtract", "â­ï¸ $name RPD å·²æ»¿ (${rpdList.size}/$rpdLimit)")
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
            sharedPrefs.edit { putStringSet("rpd_$name", rpdList.map { it.toString() }.toSet()) }
        }
    }

    private fun migrateOldBans() {
        val nextMidnight = todayStartMs() + 86_400_000
        models.forEach { entry ->
            val key = "quota_banned_${entry.name}"
            val existing = sharedPrefs.getLong(key, 0L)
            if (existing > nextMidnight) {
                sharedPrefs.edit { putLong(key, nextMidnight) }
                Log.d("TagExtract", "ðŸ”„ ${entry.name} ban å·²æ ¡æ­£è‡³å¤ªå¹³æ´‹åˆå¤œ")
            }
        }
    }

    private fun buildConfig(entry: ModelEntry): GenerateContentConfig? {
        if (!entry.supportsThinking) return null
        return GenerateContentConfig.builder()
            .thinkingConfig(ThinkingConfig.builder().thinkingBudget(0).build())
            .build()
    }

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.IO) {
        ensureOffset()
        migrateOldBans()

        val prompt = """
            è«‹å¾žä»¥ä¸‹æ–‡å­—ä¸­æå– 4 å€‹æœ€æ ¸å¿ƒçš„é—œéµå­—æ¨™ç±¤ã€‚
            è¦æ±‚ï¼š
            1. åªå›žå‚³æ¨™ç±¤å…§å®¹ï¼Œç”¨é€—è™Ÿåˆ†éš” (ä¾‹å¦‚ï¼šå°ç©é›»,è‚¡ç¥¨,æŠ•è³‡,ç†è²¡)ã€‚
            2. ä¸è¦åŒ…å«ä»»ä½•è§£é‡‹ã€åºè™Ÿæˆ–ç¬¦è™Ÿã€‚
            3. è™•ç†ã€Œå°ç©é›»ã€ã€ã€Œæ¯”ç‰¹å¹£ã€ç­‰ç†±é–€å°ˆæœ‰åè©žæ™‚ï¼Œè«‹å‹™å¿…å®Œæ•´ä¿ç•™ï¼Œä¸è¦æ‹†é–‹ã€‚
            
            æ–‡å­—å…§å®¹ï¼š$text
        """.trimIndent()

        val hasAnyAvailableModel = models.any { canUseModel(it.name, it.rpmLimit, it.rpdLimit) }
        if (!hasAnyAvailableModel) {
            Log.w("TagExtract", "æ‰€æœ‰æ¨¡åž‹é…é¡çš†å·²æ»¿ï¼Œç„¡æ³•ç™¼èµ·è«‹æ±‚")
            throw IllegalStateException("AI æœå‹™é…é¡å·²æ»¿ï¼Œè«‹ç¨å¾Œé‡è©¦æˆ–æ‰‹å‹•è¼¸å…¥æ¨™ç±¤")
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
                val responseText = response.text() ?: throw Exception("ç©ºå›žæ‡‰")
                Log.d("TagExtract", "âœ… ä½¿ç”¨æ¨¡åž‹: ${entry.name} (RPM ${entry.rpmLimit} / RPD ${entry.rpdLimit})")
                return@withContext responseText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(4)
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                Log.w("TagExtract", "âŒ æ¨¡åž‹ ${entry.name} å¤±æ•—: ${e.message}")
                val errorMsg = e.message ?: ""
                if (errorMsg.contains("Quota exceeded", ignoreCase = true) || errorMsg.contains("429")) {
                    val banUntil = todayStartMs() + 86_400_000
                    sharedPrefs.edit { putLong("quota_banned_${entry.name}", banUntil) }
                    Log.d("TagExtract", "ðŸš« ${entry.name} å·²å°éŽ–è‡³å¤ªå¹³æ´‹åˆå¤œ")
                }
            }
        }
        Log.e("TagExtract", "æ‰€æœ‰å˜—è©¦çš„æ¨¡åž‹çš†å¤±æ•—")
        throw IllegalStateException("AI æ¨™ç±¤æå–å¤±æ•—ï¼Œè«‹æ‰‹å‹•è¼¸å…¥æ¨™ç±¤")
    }
}

