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

// âœ… æ•æ„Ÿè³‡è¨Šç¾å·²ç§»è‡³ BuildConfigï¼ˆé€éŽ build.gradle.kts æ³¨å…¥ï¼‰
// åŽŸå§‹ç¡¬ç·¨ç¢¼ URL å·²ç§»é™¤ï¼Œæ”¹ç”±ç·¨è­¯æ™‚é€šéŽ buildConfigField æä¾›
// const val SHEET_WEBHOOK_URL = "..."  // â† å·²ç§»é™¤
// const val SPREADSHEET_ID = "..."      // â† å·²ç§»é™¤

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
            conn.responseCode // ç­‰å¾…å›žæ‡‰å®Œæˆ
        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
            e.printStackTrace()
        } finally {
            conn.disconnect()
        }
    }
}

// ä¿®å¾©ï¼šå¾ž testUpload æ”¹åç‚ºèªžæ„æ­£ç¢ºçš„ uploadChatAndComplete
// ä¿®å¾©ï¼šbuildString è£¡çš„äº‚ç¢¼ï¼ˆåŽŸç‚º BIG5/UTF-8 æ··ç¢¼ï¼‰+ $è®Šæ•¸ æ’å€¼å¤±æ•ˆ
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
            appendLine("å•é¡Œï¼š$questionText")       // ä¿®å¾©ï¼šåŽŸæœ¬æ˜¯äº‚ç¢¼ "??åš—?questionText"
            appendLine("å°ˆå®¶ IDï¼š$expertId")        // ä¿®å¾©ï¼šåŽŸæœ¬æ˜¯äº‚ç¢¼ "æ’ æŒ¯ IDåš—?expertId"
            appendLine("å°è©±ç´€éŒ„ï¼š")                 // ä¿®å¾©ï¼šåŽŸæœ¬æ˜¯äº‚ç¢¼ "æ’ åº—è??"
            append(messages.joinToString("\n"))
        }

        uploadToGoogleSheet(fullText)
        questionRef.child("status").setValue("completed").await()
    } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
        e.printStackTrace()
    }
}

// å·²ç§»é™¤ï¼šsubmitRatingAndCommentï¼ˆå¾žæœªè¢«å‘¼å«ï¼Œç‚ºæ­»ç¢¼ï¼‰
// è‹¥å¾ŒçºŒè¦å¯¦ä½œè©•åˆ†åŠŸèƒ½ï¼Œå¯åœ¨æ­¤è™•é‡æ–°åŠ å…¥

