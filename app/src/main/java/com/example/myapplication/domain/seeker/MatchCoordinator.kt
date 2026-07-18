package com.example.myapplication.domain.seeker

import android.util.Log
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchCoordinator(
    private val firebaseDb: FirebaseDatabase,
    private val matchingRepository: MatchingRepositoryInterface,
    private val aiRepository: AiRepository,
) {
    companion object {
        private const val MATCH_TIMEOUT_MS = 300_000L
        private const val AI_PREVIEW_DELAY_MS = 3_000L
    }

    private var matchTimeoutJob: Job? = null

    var onAiChatroomReady: ((chatroomId: String, questionText: String) -> Unit)? = null

    fun matchAndAssignExpert(questionId: String, text: String, userId: String) {
        matchingRepository.matchAndAssignExpert(questionId, text, userId)
    }

    fun startMatchTimeout(questionId: String, scope: CoroutineScope) {
        cancelMatchTimeout()
        matchTimeoutJob = scope.launch {
            delay(MATCH_TIMEOUT_MS)
            val ref = firebaseDb.getReference("questions").child(questionId)
            ref.child("status").get().addOnSuccessListener { status ->
                val s = status.value?.toString()
                if (s == "matching") {
                    ref.child("status").setValue("cancelled")
                }
            }
        }
    }

    fun startAiPreview(questionId: String, questionText: String, scope: CoroutineScope) {
        scope.launch {
            delay(AI_PREVIEW_DELAY_MS)
            val ref = firebaseDb.getReference("questions").child(questionId)
            ref.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) return@addOnSuccessListener
                val status = snapshot.child("status").value?.toString()
                val expertId = snapshot.child("expertId").value?.toString().orEmpty()
                if (status == "matching" && expertId.isBlank()) {
                    scope.launch {
                        try {
                            val answer = aiRepository.generateResponse(questionText)
                            aiRepository.createAiChatroom(questionId, questionText, answer) { chatroomId ->
                                onAiChatroomReady?.invoke(chatroomId, questionText)
                            }
                        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                            Log.w("MatchCoordinator", "AI preview failed", e)
                        }
                    }
                }
            }
        }
    }

    fun cancelMatchTimeout() {
        matchTimeoutJob?.cancel()
        matchTimeoutJob = null
    }

    fun cleanup() {
        cancelMatchTimeout()
    }
}

