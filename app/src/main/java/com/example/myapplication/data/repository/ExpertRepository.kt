package com.example.myapplication.data.repository

import com.example.myapplication.data.FirebaseFields
import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.StatusValues
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpertRepository(private val db: FirebaseDatabase) {

    suspend fun saveSkill(userId: String, expertise: String): String {
        val ref = db.getReference(FirebasePaths.SOLUTIONS).child(userId).push()
        val skillId = ref.key ?: throw Exception("無法建立技能 ID")
        val now = System.currentTimeMillis()

        val updates = mapOf(
            "${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.ID}" to skillId,
            "${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.EXPERTISE}" to expertise,
            "${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.TAGS}" to emptyList<String>(),
            "${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.TIMESTAMP}" to now,
            "${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.STATUS}" to SkillStatus.PENDING.name,
            "${FirebasePaths.PENDING_SKILLS}/$skillId/${FirebaseFields.USER_ID}" to userId,
            "${FirebasePaths.PENDING_SKILLS}/$skillId/${FirebaseFields.TEXT}" to expertise,
            "${FirebasePaths.PENDING_SKILLS}/$skillId/${FirebaseFields.TIMESTAMP}" to now
        )

        db.getReference().updateChildren(updates).await()
        return skillId
    }

    suspend fun editSkill(userId: String, skillId: String, newExpertise: String) {
        val updates = mutableMapOf<String, Any>()
        updates["${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.EXPERTISE}"] = newExpertise
        updates["${FirebasePaths.SOLUTIONS}/$userId/$skillId/${FirebaseFields.STATUS}"] = SkillStatus.PENDING.name

        val queueData = mapOf(
            FirebaseFields.USER_ID to userId,
            FirebaseFields.TEXT to newExpertise,
            FirebaseFields.TIMESTAMP to System.currentTimeMillis()
        )
        updates["${FirebasePaths.PENDING_SKILLS}/$skillId"] = queueData

        db.getReference().updateChildren(updates).await()
    }

    fun listenToSolutionHistory(userId: String): Flow<List<SolutionItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val history = snapshot.children.mapNotNull { child ->
                    val expertise = child.child(FirebaseFields.EXPERTISE).getValue(String::class.java)
                        ?: return@mapNotNull null
                    SolutionItem(
                        id = child.key ?: "",
                        questionId = child.child(FirebaseFields.QUESTION_ID).getValue(String::class.java) ?: "",
                        expertise = expertise,
                        tags = child.child(FirebaseFields.TAGS).children.mapNotNull { it.getValue(String::class.java) },
                        timestamp = child.child(FirebaseFields.TIMESTAMP).getValue(Long::class.java) ?: 0L,
                        status = SkillStatus.fromName(child.child(FirebaseFields.STATUS).getValue(String::class.java))
                    )
                }
                trySend(history)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.getReference(FirebasePaths.SOLUTIONS).child(userId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeExpertStatus(userId: String): Flow<Pair<Double, Long>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(5.0 to 0L)
            close()
            return@callbackFlow
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rating = snapshot.child(FirebaseFields.RATING).getValue(Double::class.java) ?: 5.0
                val helpCount = snapshot.child(FirebaseFields.HELP_COUNT).getValue(Long::class.java) ?: 0L
                trySend(rating to helpCount)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.getReference(FirebasePaths.USERS).child(userId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun publishExperience(userId: String, text: String): String {
        val expRef = db.getReference(FirebasePaths.ACTIVE_EXPERIENCES).push()
        val expId = expRef.key ?: throw Exception("無法建立經驗 ID")

        val experienceData = mapOf(
            FirebaseFields.EXPERT_ID to userId,
            FirebaseFields.TEXT to text,
            FirebaseFields.TIMESTAMP to System.currentTimeMillis(),
            FirebaseFields.STATUS to StatusValues.ACTIVE,
            FirebaseFields.IS_ONLINE to true
        )

        expRef.setValue(experienceData).await()
        return expId
    }

    suspend fun editExperience(experienceId: String, newText: String) {
        if (experienceId.isBlank()) throw IllegalArgumentException("無效的經驗 ID")
        db.getReference(FirebasePaths.ACTIVE_EXPERIENCES)
            .child(experienceId)
            .child(FirebaseFields.TEXT)
            .setValue(newText).await()
    }

    fun stopExperience(experienceId: String) {
        if (experienceId.isNotBlank()) {
            db.getReference(FirebasePaths.ACTIVE_EXPERIENCES).child(experienceId).removeValue()
        }
    }

    fun setExpertOnline(online: Boolean, userId: String, experienceId: String) {
        db.getReference(FirebasePaths.USERS).child(userId).child(FirebaseFields.IS_ONLINE).setValue(online)
        if (experienceId.isNotBlank()) {
            val expRef = db.getReference(FirebasePaths.ACTIVE_EXPERIENCES).child(experienceId)
            expRef.child(FirebaseFields.STATUS).setValue(if (online) StatusValues.ACTIVE else StatusValues.OFFLINE)
            expRef.child(FirebaseFields.IS_ONLINE).setValue(online)
        }
    }

    fun cleanup(activeExperienceId: String) {
        if (activeExperienceId.isNotBlank()) {
            stopExperience(activeExperienceId)
        }
    }
}
