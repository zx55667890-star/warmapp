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
            你是一個專業的搜尋系統標籤生成器。請根據以下真人專家輸入的專業領域，提煉出 3 到 5 個精準的「關鍵字特徵標籤」(Tags)，用來幫助配對系統搜尋。
            
            大領域：${domain}
            子領域：${subDomain}
            具體能解決的問題：${problem}

            規則：
            1. 直接回覆標籤名稱，使用半形逗號 (,) 分隔。
            2. 絕對不要加上 # 字號。
            3. 絕對不要包含任何其他解釋或問候文字。
            
            輸出範例：淘寶,跨境退貨,海運物流,兩岸電商
        """.trimIndent()
        return@withContext try {
            Log.d("AiRepo", "generateExpertTags: sending prompt length=${prompt.length}")
            val response = client.models.generateContent("gemini-2.0-flash", prompt, emptyConfig)
            val responseText = response.text()
            if (!responseText.isNullOrBlank()) {
                val tags = responseText!!.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d("AiRepo", "generateExpertTags: parsed tags=$tags")
                tags
            } else {
                Log.w("AiRepo", "generateExpertTags: empty response, using fallback")
                generateLocalFallbackTags(domain, subDomain, problem)
            }
        } catch (e: Exception) {
            Log.e("AiRepo", "Gemini API 限流或異常，啟動本地降級斷詞機制: ${e.message}")
            generateLocalFallbackTags(domain, subDomain, problem)
        }
    }

    private fun generateLocalFallbackTags(domain: String, subDomain: String, problem: String): List<String> {
        val fallbackTags = mutableListOf<String>()
        if (domain.isNotBlank()) fallbackTags.add(domain.trim())
        if (subDomain.isNotBlank()) fallbackTags.add(subDomain.trim())
        if (problem.isNotBlank()) {
            val tokens = problem.split(Regex("[\\s,，、\\.。到從的與和去在]"))
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
        val prompt = "你是一個經驗分享平台的AI助手。請針對以下問題提供實用、具體的建議。" +
                "回答請用繁體中文，控制在300字以內。問題：$question"
        val response = client.models.generateContent("gemini-2.0-flash", prompt, emptyConfig)
        response.text() ?: "抱歉，我暫時無法回答這個問題。"
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
