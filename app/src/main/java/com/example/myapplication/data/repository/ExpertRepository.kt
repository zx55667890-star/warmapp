package com.example.myapplication.data.repository

import com.example.myapplication.data.model.SolutionItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ExpertRepository(private val db: FirebaseDatabase) {

    private var statusListener: ValueEventListener? = null
    private var currentUserId: String? = null

    /** 儲存解法並更新 helpCount，失敗時拋出 Exception。 */
    suspend fun saveSolution(
        userId: String,
        questionId: String,
        expertise: String,
        tags: List<String>
    ) = suspendCancellableCoroutine<Unit> { cont ->
        val solutionRef = db.getReference("solutions").child(userId).push()
        val solutionId = solutionRef.key
        if (solutionId == null) {
            cont.resumeWithException(Exception("無法建立方案 ID"))
            return@suspendCancellableCoroutine
        }

        val solutionData = mapOf(
            "questionId" to questionId,
            "expertise" to expertise,
            "tags" to tags,
            "timestamp" to System.currentTimeMillis()
        )

        val updates = mapOf(
            "solutions/$userId/$solutionId" to solutionData,
            "users/$userId/helpCount" to com.google.firebase.database.ServerValue.increment(1)
        )

        db.reference.updateChildren(updates)
            .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
            .addOnFailureListener { if (cont.isActive) cont.resumeWithException(Exception(it.message ?: "儲存失敗")) }
    }

    fun listenToSolutionHistory(userId: String, onHistoryUpdate: (List<SolutionItem>) -> Unit) {
        db.getReference("solutions").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val history = snapshot.children.mapNotNull { child ->
                        val expertise = child.child("expertise").getValue(String::class.java)
                            ?: return@mapNotNull null
                        SolutionItem(
                            id = child.key ?: "",
                            questionId = child.child("questionId").getValue(String::class.java) ?: "",
                            expertise = expertise,
                            tags = child.child("tags").children.mapNotNull { it.getValue(String::class.java) },
                            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        )
                    }
                    onHistoryUpdate(history)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun initializeExpertStatus(userId: String, onUpdate: (rating: Double, helpCount: Long) -> Unit) {
        currentUserId = userId
        val userRef = db.getReference("users").child(userId)

        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rating = snapshot.child("rating").getValue(Double::class.java) ?: 5.0
                val helpCount = snapshot.child("helpCount").getValue(Long::class.java) ?: 0L
                onUpdate(rating, helpCount)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        userRef.addValueEventListener(statusListener!!)
    }

    /** 發佈新的專家經驗，成功後回傳新建的 experienceId。 */
    suspend fun publishExperience(userId: String, text: String): String =
        suspendCancellableCoroutine { cont ->
            val expRef = db.getReference("active_experiences").push()
            val expId = expRef.key
            if (expId == null) {
                cont.resumeWithException(Exception("無法建立經驗 ID"))
                return@suspendCancellableCoroutine
            }

            val experienceData = mapOf(
                "expertId" to userId,
                "text" to text,
                "timestamp" to System.currentTimeMillis(),
                "status" to "active",
                "isOnline" to true
            )

            expRef.setValue(experienceData)
                .addOnSuccessListener { if (cont.isActive) cont.resume(expId) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(Exception(it.message ?: "發佈失敗")) }
        }

    /** 編輯既有的專家經驗文字，失敗時拋出 Exception。 */
    suspend fun editExperience(experienceId: String, newText: String) {
        if (experienceId.isBlank()) throw IllegalArgumentException("無效的經驗 ID")
        suspendCancellableCoroutine<Unit> { cont ->
            db.getReference("active_experiences")
                .child(experienceId)
                .child("text")
                .setValue(newText)
                .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                .addOnFailureListener {
                    if (cont.isActive) cont.resumeWithException(Exception(it.message ?: "更新失敗，請稍後再試"))
                }
        }
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