package com.example.myapplication.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * QuestionRepository - 負責問題（提問）相關的 Firebase 操作
 */
class QuestionRepository(private val firebaseDb: FirebaseDatabase) {

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
        val data = mapOf(
            "text" to text,
            "status" to "matching",
            "timestamp" to System.currentTimeMillis(),
            "authorId" to userId,
            "expertId" to ""
        )
        newRef.setValue(data).addOnSuccessListener {
            callback.onSent(id)
        }.addOnFailureListener { e ->
            callback.onError(e.message ?: "問題發送失敗")
        }
    }

    // =============================================================
    // Listen to Question Status
    // =============================================================
    interface QuestionStatusListener {
        fun onPendingAcceptance()
        fun onExpertAccepted()
        fun onTaken(expertId: String, questionText: String)
        fun onNoExperts()
        fun onCancelled()
        fun onMatching()
    }

    private var statusListener: ValueEventListener? = null
    private var statusRef: DatabaseReference? = null

    fun startListening(questionId: String, listener: QuestionStatusListener) {
        // Ensure any previous listener is removed
        stopListening()
        statusRef = firebaseDb.getReference("questions").child(questionId)
        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                when (snapshot.child("status").value?.toString()) {
                    "expert_accepted" -> {
                        val expertId = snapshot.child("expertId").value?.toString().orEmpty()
                        listener.onExpertAccepted()
                    }
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
    // Accept / Reject Expert Match
    // =============================================================
    fun acceptExpertMatch(questionId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseDb.getReference("questions").child(questionId).updateChildren(
            mapOf("status" to "taken")
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "接受匹配失敗") }
    }

    fun rejectExpertMatch(questionId: String, userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val qRef = firebaseDb.getReference("questions").child(questionId)
        qRef.child("rejectedExperts").child(userId).setValue(true).addOnSuccessListener {
            qRef.updateChildren(mapOf(
                "status" to "matching",
                "expertId" to "",
                "matchedExpText" to "",
                "matchedExpTimestamp" to 0
            )).addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "拒絕匹配失敗") }
        }.addOnFailureListener { e -> onError(e.message ?: "拒絕匹配失敗") }
    }

    // =============================================================
    // Cancel Matching
    // =============================================================
    fun cancelMatching(questionId: String, onComplete: () -> Unit) {
        firebaseDb.getReference("questions").child(questionId).child("status").setValue("cancelled")
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    // =============================================================
    // Rating Transaction (formerly in ViewModel)
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
    // Reconnection check (used on app start)
    // =============================================================
    interface ReconnectionListener {
        fun onExpertChatActive(chatId: String, questionText: String)
        fun onUserReconnected(questionId: String, status: String, questionText: String)
    }

    fun checkReconnection(userId: String, listener: ReconnectionListener) {
        // Check if there is a chat where the expert is already matched (status "taken")
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
                    // not expert, check user side
                        listener.onUserReconnected("", "", "") // placeholder, actual handling done by another method
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w("QuestionRepository", "Reconnection query cancelled", error.toException())
                }
            })
    }
}
