package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.FirebaseFields
import com.example.myapplication.data.FirebasePaths
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * QuestionRepository - 負責問題（提問）相關的 Firebase 操作
 */
class QuestionRepository(private val firebaseDb: FirebaseDatabase) {

    // =============================================================
    // 🛡️ 新增：提問次數與狀態防禦性檢查
    // =============================================================
    
    /**
     * 獲取使用者今日（自 00:00 起）已發送的有效提問總數（不包含已取消的）
     */
    suspend fun getTodayQuestionCount(userId: String): Int = suspendCoroutine { continuation ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        firebaseDb.getReference("questions")
            .orderByChild("authorId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0
                    for (child in snapshot.children) {
                        val timestamp = child.child("timestamp").value as? Long ?: 0L
                        val status = child.child("status").value?.toString()
                        
                        // 只計算今天之內、且沒有被取消的提問
                        if (timestamp >= startOfDay && status != "cancelled") {
                            count++
                        }
                    }
                    continuation.resume(count)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("QuestionRepository", "getTodayQuestionCount cancelled", error.toException())
                    continuation.resume(0) // 發生錯誤時保險回傳 0，不卡死正常使用者
                }
            })
    }

    /**
     * 檢查使用者當前是否有任何「進行中/媒合中」的提問（防止重複多開對話）
     */
    suspend fun hasActiveQuestion(userId: String): Boolean = suspendCoroutine { continuation ->
        firebaseDb.getReference("questions")
            .orderByChild("authorId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val hasActive = snapshot.children.any { child ->
                        val status = child.child("status").value?.toString()
                        // 正在媒合或是已進入聊天室(taken)都算進行中
                        status == "matching" || status == "taken"
                    }
                    continuation.resume(hasActive)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("QuestionRepository", "hasActiveQuestion cancelled", error.toException())
                    continuation.resume(false)
                }
            })
    }

    // =============================================================
    // Send Question
    // =============================================================
    interface SendQuestionCallback {
        fun onSent(questionId: String)
        fun onError(message: String)
    }

    fun sendQuestion(text: String, userId: String, callback: SendQuestionCallback) {
        val newRef = firebaseDb.getReference("questions").push()
        val id = newRef.key ?: return
        val now = System.currentTimeMillis()
        val data = mapOf(
            "text" to text,
            "status" to "matching",
            "timestamp" to now,
            "authorId" to userId,
            "expertId" to ""
        )
        newRef.setValue(data).addOnSuccessListener {
            firebaseDb.getReference(FirebasePaths.PENDING_QUESTIONS).child(id)
                .setValue(mapOf(
                    FirebaseFields.USER_ID to userId,
                    FirebaseFields.TEXT to text,
                    FirebaseFields.TIMESTAMP to now
                ))
            callback.onSent(id)
        }.addOnFailureListener { e ->
            callback.onError(e.message ?: "問題發送失敗")
        }
    }

    // =============================================================
    // Listen to Question Status
    // =============================================================
    interface QuestionStatusListener {
        fun onTaken(expertId: String, questionText: String)
        fun onNoExperts()
        fun onCancelled()
        fun onMatching()
    }

    private var statusListener: ValueEventListener? = null
    private var statusRef: DatabaseReference? = null

    fun startListening(questionId: String, listener: QuestionStatusListener) {
        stopListening()
        statusRef = firebaseDb.getReference("questions").child(questionId)
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                when (snapshot.child("status").value?.toString()) {
                    "taken" -> {
                        val expertId = snapshot.child("expertId").value?.toString().orEmpty()
                        val text = snapshot.child("text").value?.toString().orEmpty()
                        listener.onTaken(expertId, text)
                    }
                    "no_experts" -> listener.onNoExperts()
                    "cancelled" -> listener.onCancelled()
                    "matching" -> listener.onMatching()
                    else -> { /* ignore unknown status */ }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("QuestionRepository", "Status listener cancelled", error.toException())
            }
        }
        statusRef?.addValueEventListener(statusListener!!)
    }

    fun stopListening() {
        statusRef?.let { ref ->
            statusListener?.let { ref.removeEventListener(it) }
        }
        statusRef = null
        statusListener = null
    }

    // =============================================================
    // Cancel Matching
    // =============================================================
    fun cancelMatching(questionId: String, onComplete: () -> Unit) {
        val questionRef = firebaseDb.getReference("questions").child(questionId)
        val chatroomRef = firebaseDb.getReference("chatrooms").child("ai_$questionId")
        val pendingRef = firebaseDb.getReference(FirebasePaths.PENDING_QUESTIONS).child(questionId)
        pendingRef.removeValue()
        questionRef.removeValue()
            .addOnSuccessListener {
                chatroomRef.removeValue()
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener { e ->
                        Log.e("CancelMatch", "chatroom remove failed", e)
                        onComplete()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CancelMatch", "question remove failed", e)
                onComplete()
            }
    }

    // =============================================================
    // Rating Transaction
    // =============================================================
    interface RatingCallback {
        fun onSuccess()
        fun onError(message: String)
    }

    fun submitRating(
        expertId: String,
        score: Double,
        comment: String = "",
        activeChatRoomId: String,
        callback: RatingCallback
    ) {
        if (expertId.isBlank()) { callback.onSuccess(); return }
        val expertRef = firebaseDb.getReference("experts").child(expertId)
        expertRef.getParent()?.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val expertData = mutableData.child(expertId)
                val oldRating = (expertData.child("rating").value as? Number)?.toDouble() ?: 5.0
                val oldCount = (expertData.child("ratingCount").value as? Number)?.toLong() ?: 0L
                val newCount = oldCount + 1
                val newRating = (oldRating * oldCount + score) / newCount
                expertData.child("rating").value = newRating
                expertData.child("ratingCount").value = newCount
                if (comment.isNotBlank()) {
                    expertData.child("reviews").child(activeChatRoomId).child("score").value = score
                    expertData.child("reviews").child(activeChatRoomId).child("comment").value = comment
                    expertData.child("reviews").child(activeChatRoomId).child("timestamp").value = System.currentTimeMillis()
                }
                return Transaction.success(mutableData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    callback.onError(error.message ?: "評分失敗")
                } else if (committed) {
                    callback.onSuccess()
                } else {
                    callback.onError("評分未提交，請再試一次")
                }
            }
        })
    }

    // =============================================================
    // Reconnection check
    // =============================================================
    interface ReconnectionListener {
        fun onExpertChatActive(chatId: String, questionText: String)
        fun onUserReconnected(questionId: String, status: String, questionText: String)
    }

    fun checkReconnection(userId: String, listener: ReconnectionListener) {
        firebaseDb.getReference("questions")
            .orderByChild("expertId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val takenQuestion = snapshot.children.firstOrNull {
                        it.child("status").value?.toString() == "taken"
                    }
                    if (takenQuestion != null) {
                        listener.onExpertChatActive(
                            takenQuestion.key.orEmpty(),
                            takenQuestion.child("text").value?.toString().orEmpty()
                        )
                        return
                    }
                    listener.onUserReconnected("", "", "")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("QuestionRepository", "Reconnection query cancelled", error.toException())
                }
            })
    }
}
