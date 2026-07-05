package com.example.myapplication.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpertRepository(private val db: FirebaseDatabase) {

    interface ExpertStatusCallback {
        fun onStatusUpdated(rating: Double, helpCount: Long)
    }

    private var statusListener: ValueEventListener? = null
    private var currentUserId: String? = null

    // 新增：儲存解法至知識庫
    fun saveSolution(userId: String, questionId: String, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val solutionRef = db.getReference("solutions").child(userId).push()
        val solutionId = solutionRef.key ?: return

        val solutionData = mapOf(
            "questionId" to questionId,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        // 原子性操作：存入解法 + 增加協助計數
        val updates = mapOf(
            "solutions/$userId/$solutionId" to solutionData,
            "users/$userId/helpCount" to com.google.firebase.database.ServerValue.increment(1)
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "儲存失敗") }
    }

    fun listenToSolutionHistory(userId: String, onHistoryUpdate: (List<String>) -> Unit) {
        db.getReference("solutions").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull { it.child("content").getValue(String::class.java) }
                    onHistoryUpdate(history)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun initializeExpertStatus(userId: String, callback: ExpertStatusCallback) {
        currentUserId = userId
        val userRef = db.getReference("users").child(userId)

        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rating = snapshot.child("rating").getValue(Double::class.java) ?: 5.0
                val helpCount = snapshot.child("helpCount").getValue(Long::class.java) ?: 0L
                callback.onStatusUpdated(rating, helpCount)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        }
        userRef.addValueEventListener(statusListener!!)
    }

    fun publishExperience(userId: String, text: String, onSuccess: (String) -> Unit) {
        val expRef = db.getReference("active_experiences").push()
        val expId = expRef.key ?: return

        val experienceData = mapOf(
            "expertId" to userId,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "status" to "active",
            "isOnline" to true
        )

        expRef.setValue(experienceData).addOnSuccessListener {
            onSuccess(expId)
        }
    }

    fun editExperience(experienceId: String, newText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (experienceId.isBlank()) {
            onError("無效的經驗 ID")
            return
        }

        db.getReference("active_experiences")
            .child(experienceId)
            .child("text")
            .setValue(newText)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "更新失敗，請稍後再試") }
    }

    fun stopExperience(experienceId: String) {
        if (experienceId.isNotBlank()) {
            db.getReference("active_experiences").child(experienceId).removeValue()
        }
    }

    fun setExpertOnline(online: Boolean, experienceId: String) {
        currentUserId?.let { uid ->
            db.getReference("users").child(uid).child("isOnline").setValue(online)

            if (experienceId.isNotBlank()) {
                val expRef = db.getReference("active_experiences").child(experienceId)
                expRef.child("status").setValue(if (online) "active" else "offline")
                expRef.child("isOnline").setValue(online)
            }
        }
    }

    fun cleanup(activeExperienceId: String) {
        currentUserId?.let { uid ->
            statusListener?.let { db.getReference("users").child(uid).removeEventListener(it) }
        }
        if (activeExperienceId.isNotBlank()) {
            stopExperience(activeExperienceId)
        }
    }
}