package com.example.myapplication.data.repository

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
