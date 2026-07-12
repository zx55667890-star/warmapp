package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.firebase.database.FirebaseDatabase
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository(
    private val firebaseDb: FirebaseDatabase
) {
    private val client = Client.builder().apiKey(BuildConfig.GEMINI_API_KEY).build()
    private val emptyConfig = GenerateContentConfig.builder().build()

    suspend fun generateExpertTags(domain: String, subDomain: String, problem: String): List<String> = withContext(Dispatchers.IO) {
        val prompt = """
            ä½ æ˜¯ä¸€å€‹å°ˆæ¥­çš„æœå°‹ç³»çµ±æ¨™ç±¤ç”Ÿæˆå™¨ã€‚è«‹æ ¹æ“šä»¥ä¸‹çœŸäººå°ˆå®¶è¼¸å…¥çš„å°ˆæ¥­é ˜åŸŸï¼Œæç…‰å‡º 3 åˆ° 5 å€‹ç²¾æº–çš„ã€Œé—œéµå­—ç‰¹å¾µæ¨™ç±¤ã€(Tags)ï¼Œç”¨ä¾†å¹«åŠ©é…å°ç³»çµ±æœå°‹ã€‚
            
            å¤§é ˜åŸŸï¼š${domain}
            å­é ˜åŸŸï¼š${subDomain}
            å…·é«”èƒ½è§£æ±ºçš„å•é¡Œï¼š${problem}

            è¦å‰‡ï¼š
            1. ç›´æŽ¥å›žè¦†æ¨™ç±¤åç¨±ï¼Œä½¿ç”¨åŠå½¢é€—è™Ÿ (,) åˆ†éš”ã€‚
            2. çµ•å°ä¸è¦åŠ ä¸Š # å­—è™Ÿã€‚
            3. çµ•å°ä¸è¦åŒ…å«ä»»ä½•å…¶ä»–è§£é‡‹æˆ–å•å€™æ–‡å­—ã€‚
            
            è¼¸å‡ºç¯„ä¾‹ï¼šæ·˜å¯¶,è·¨å¢ƒé€€è²¨,æµ·é‹ç‰©æµ,å…©å²¸é›»å•†
        """.trimIndent()
        return@withContext try {
            Log.d("AiRepo", "generateExpertTags: sending prompt length=${prompt.length}")
            val response = client.models.generateContent("gemini-2.0-flash", prompt, emptyConfig)
            val responseText = response.text()
            if (!responseText.isNullOrBlank()) {
                val tags = responseText!!.split(",", "ï¼Œ").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d("AiRepo", "generateExpertTags: parsed tags=$tags")
                tags
            } else {
                Log.w("AiRepo", "generateExpertTags: empty response, using fallback")
                generateLocalFallbackTags(domain, subDomain, problem)
            }
        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
            Log.e("AiRepo", "Gemini API é™æµæˆ–ç•°å¸¸ï¼Œå•Ÿå‹•æœ¬åœ°é™ç´šæ–·è©žæ©Ÿåˆ¶: ${e.message}")
            generateLocalFallbackTags(domain, subDomain, problem)
        }
    }

    private fun generateLocalFallbackTags(domain: String, subDomain: String, problem: String): List<String> {
        val fallbackTags = mutableListOf<String>()
        if (domain.isNotBlank()) fallbackTags.add(domain.trim())
        if (subDomain.isNotBlank()) fallbackTags.add(subDomain.trim())
        if (problem.isNotBlank()) {
            val tokens = problem.split(Regex("[\\s,ï¼Œã€\\.ã€‚åˆ°å¾žçš„èˆ‡å’ŒåŽ»åœ¨]"))
            tokens.forEach { token ->
                val cleaned = token.trim()
                if (cleaned.length >= 2 && !cleaned.all { it.isDigit() }) {
                    if (!fallbackTags.contains(cleaned)) fallbackTags.add(cleaned)
                }
            }
        }
        return fallbackTags.take(5)
    }

    suspend fun generateResponse(question: String): String = withContext(Dispatchers.IO) {
        val prompt = "ä½ æ˜¯ä¸€å€‹ç¶“é©—åˆ†äº«å¹³å°çš„AIåŠ©æ‰‹ã€‚è«‹é‡å°ä»¥ä¸‹å•é¡Œæä¾›å¯¦ç”¨ã€å…·é«”çš„å»ºè­°ã€‚" +
                "å›žç­”è«‹ç”¨ç¹é«”ä¸­æ–‡ï¼ŒæŽ§åˆ¶åœ¨300å­—ä»¥å…§ã€‚å•é¡Œï¼š$question"
        val response = client.models.generateContent("gemini-2.0-flash", prompt, emptyConfig)
        response.text() ?: "æŠ±æ­‰ï¼Œæˆ‘æš«æ™‚ç„¡æ³•å›žç­”é€™å€‹å•é¡Œã€‚"
    }

    fun createAiChatroom(questionId: String, questionText: String, aiResponse: String, onComplete: (String) -> Unit) {
        val chatroomId = "ai_$questionId"
        val chatroomRef = firebaseDb.getReference("chatrooms").child(chatroomId)
        val messagesRef = chatroomRef.child("messages")
        val timestamp = System.currentTimeMillis()

        val userMsgId = messagesRef.push().key ?: return
        val aiMsgId = messagesRef.push().key ?: return

        val userMessage = mapOf(
            "senderId" to "",
            "sender" to "user",
            "text" to questionText,
            "timestamp" to timestamp,
            "readBy" to mapOf("system" to true)
        )
        val aiMessage = mapOf(
            "senderId" to "ai_assistant",
            "sender" to "ai",
            "text" to aiResponse,
            "timestamp" to timestamp + 1,
            "readBy" to mapOf("system" to true)
        )

        chatroomRef.child("status").setValue("active")
        messagesRef.child(userMsgId).setValue(userMessage)
        messagesRef.child(aiMsgId).setValue(aiMessage)
            .addOnSuccessListener { onComplete(chatroomId) }
    }
}

