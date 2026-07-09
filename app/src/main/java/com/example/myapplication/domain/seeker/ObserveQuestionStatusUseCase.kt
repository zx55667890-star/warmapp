package com.example.myapplication.domain.seeker

import android.util.Log
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class QuestionStatus {
    data class Taken(val questionId: String, val questionText: String) : QuestionStatus()
    data class ExpertAccepted(val expertId: String, val expertText: String, val timestamp: Long) : QuestionStatus()
    data object NoExperts : QuestionStatus()
    data class Cancelled(val authorId: String) : QuestionStatus()
    data object Matching : QuestionStatus()
}

class ObserveQuestionStatusUseCase(
    private val firebaseDb: FirebaseDatabase
) {
    operator fun invoke(questionId: String): Flow<QuestionStatus> = callbackFlow {
        val ref: DatabaseReference = firebaseDb.getReference("questions").child(questionId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").value?.toString() ?: return
                val statusEvent = when (status) {
                    "taken" -> QuestionStatus.Taken(
                        questionId = questionId,
                        questionText = snapshot.child("text").value?.toString().orEmpty()
                    )
                    "expert_accepted" -> QuestionStatus.ExpertAccepted(
                        expertId = snapshot.child("expertId").value?.toString().orEmpty(),
                        expertText = snapshot.child("matchedExpText").value?.toString().orEmpty(),
                        timestamp = (snapshot.child("matchedExpTimestamp").value as? Long) ?: 0L
                    )
                    "no_experts" -> QuestionStatus.NoExperts
                    "cancelled" -> QuestionStatus.Cancelled(
                        authorId = snapshot.child("authorId").value?.toString().orEmpty()
                    )
                    "matching" -> QuestionStatus.Matching
                    else -> null
                }
                if (statusEvent != null) {
                    trySend(statusEvent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ObserveQuestionStatus", "Listener cancelled", error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }
}
