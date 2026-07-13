package com.example.myapplication.data.repository

import com.example.myapplication.data.model.SkillStatus
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

    suspend fun checkBlacklist(text: String): Boolean = suspendCancellableCoroutine { cont ->
        db.getReference("tags_blacklist").child(text).get()
            .addOnSuccessListener { snapshot ->
                if (cont.isActive) cont.resume(snapshot.exists())
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(false)
            }
    }

    suspend fun checkWhitelist(text: String): List<String>? = suspendCancellableCoroutine { cont ->
        db.getReference("tags_whitelist").child(text).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val tags = snapshot.child("tags").children.mapNotNull { it.getValue(String::class.java) }
                    if (cont.isActive) cont.resume(if (tags.isEmpty()) null else tags)
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
    }

    suspend fun saveSkill(userId: String, expertise: String, tags: List<String>, status: SkillStatus): String =
        suspendCancellableCoroutine { cont ->
            val ref = db.getReference("solutions").child(userId).push()
            val skillId = ref.key
            if (skillId == null) {
                cont.resumeWithException(Exception("無法建立技能 ID"))
                return@suspendCancellableCoroutine
            }

            if (status == SkillStatus.PENDING) {
                val queueRef = db.getReference("pending_skills").child(skillId)
                val queueData = mapOf(
                    "userId" to userId,
                    "text" to expertise,
                    "timestamp" to System.currentTimeMillis()
                )
                queueRef.setValue(queueData)
            }

            val data = mapOf<String, Any>(
                "id" to skillId,
                "expertise" to expertise,
                "tags" to tags,
                "timestamp" to System.currentTimeMillis(),
                "status" to status.name
            )

            ref.setValue(data)
                .addOnSuccessListener { if (cont.isActive) cont.resume(skillId) }
                .addOnFailureListener {
                    if (cont.isActive) cont.resumeWithException(Exception(it.message ?: "儲存失敗"))
                }
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
                            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                            status = child.child("status").getValue(String::class.java) ?: SkillStatus.ACTIVE.name
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
