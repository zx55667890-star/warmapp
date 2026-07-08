package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AiRepository(
    private val firebaseDb: FirebaseDatabase
) {
    suspend fun generateExpertTags(domain: String, subDomain: String, problem: String): List<String> = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            Log.w("AiRepo", "generateExpertTags: GEMINI_API_KEY is empty")
            return@withContext emptyList()
        }
        val prompt = """
            你是一個專業的搜尋系統標籤生成器。請根據以下真人專家輸入的專業領域，提煉出 3 到 5 個精準的「關鍵字特徵標籤」(Tags)，用來幫助配對系統搜尋。
            
            大領域：$domain
            子領域：$subDomain
            具體能解決的問題：$problem

            規則：
            1. 直接回覆標籤名稱，使用半形逗號 (,) 分隔。
            2. 絕對不要加上 # 字號。
            3. 絕對不要包含任何其他解釋或問候文字。
            
            輸出範例：淘寶,跨境退貨,海運物流,兩岸電商
        """.trimIndent()
        try {
            Log.d("AiRepo", "generateExpertTags: sending prompt length=${prompt.length}")
            val response = model.generateContent(prompt)
            val text = response.text?.trim()
            Log.d("AiRepo", "generateExpertTags: raw response=$text")
            if (text.isNullOrBlank()) {
                Log.w("AiRepo", "generateExpertTags: empty response")
                emptyList()
            } else {
                val tags = text.split(",", "，").map { it.trim() }.filter { it.isNotEmpty() }
                Log.d("AiRepo", "generateExpertTags: parsed tags=$tags")
                tags
            }
        } catch (e: Exception) {
            Log.e("AiRepo", "generateExpertTags: ${e.message}", e)
            emptyList()
        }
    }
    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateResponse(question: String): String = withContext(Dispatchers.IO) {
        val prompt = "你是一個經驗分享平台的AI助手。請針對以下問題提供實用、具體的建議。" +
                "回答請用繁體中文，控制在300字以內。問題：$question"
        val response = model.generateContent(prompt)
        response.text ?: "抱歉，我暫時無法回答這個問題。"
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
