package com.example.myapplication.util

import com.example.myapplication.BuildConfig

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ✅ 敏感資訊現已移至 BuildConfig（透過 build.gradle.kts 注入）
// 原始硬編碼 URL 已移除，改由編譯時通過 buildConfigField 提供
// const val SHEET_WEBHOOK_URL = "..."  // ← 已移除
// const val SPREADSHEET_ID = "..."      // ← 已移除

suspend fun uploadToGoogleSheet(text: String) {
    withContext(Dispatchers.IO) {
        val conn = (URL(BuildConfig.SHEET_WEBHOOK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        try {
            val jsonParam = JSONObject().apply {
                put("spreadsheetId", BuildConfig.SPREADSHEET_ID)
                put("text", text)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(jsonParam) }
            conn.responseCode // 等待回應完成
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.disconnect()
        }
    }
}

// 修復：從 testUpload 改名為語意正確的 uploadChatAndComplete
// 修復：buildString 裡的亂碼（原為 BIG5/UTF-8 混碼）+ $變數 插值失效
suspend fun uploadChatAndComplete(chatroomId: String, firebaseDb: FirebaseDatabase) {
    if (chatroomId.isBlank()) return

    try {
        val questionRef = firebaseDb.getReference("questions").child(chatroomId)
        val snapshot = questionRef.get().await()

        val questionText = snapshot.child("text").value?.toString().orEmpty()
        val expertId = snapshot.child("expertId").value?.toString().orEmpty()

        val chatSnapshot = firebaseDb.getReference("chatrooms")
            .child(chatroomId)
            .child("messages")
            .get()
            .await()

        val messages = chatSnapshot.children.mapNotNull { child ->
            val sender = child.child("sender").value?.toString().orEmpty()
            val text = child.child("text").value?.toString().orEmpty()
            if (child.key != "typing_status" && text.isNotBlank()) "[$sender]: $text" else null
        }

        val fullText = buildString {
            appendLine("問題：$questionText")       // 修復：原本是亂碼 "??嚗?questionText"
            appendLine("專家 ID：$expertId")        // 修復：原本是亂碼 "撠振 ID嚗?expertId"
            appendLine("對話紀錄：")                 // 修復：原本是亂碼 "撠店蝝??"
            append(messages.joinToString("\n"))
        }

        uploadToGoogleSheet(fullText)
        questionRef.child("status").setValue("completed").await()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 已移除：submitRatingAndComment（從未被呼叫，為死碼）
// 若後續要實作評分功能，可在此處重新加入
