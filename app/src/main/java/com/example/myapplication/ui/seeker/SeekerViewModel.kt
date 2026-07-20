package com.example.myapplication.ui.seeker

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.StatusValues
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.domain.seeker.MatchCoordinator
import com.example.myapplication.domain.seeker.ObserveQuestionStatusUseCase
import com.example.myapplication.domain.seeker.QuestionStatus
import com.example.myapplication.domain.seeker.QuotaResult
import com.example.myapplication.domain.seeker.SendQuestionMediaUseCase
import com.example.myapplication.domain.seeker.ValidateQuestionQuotaUseCase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SeekerUiState(
    val isUserMatching: Boolean = false,
    val isPendingAcceptance: Boolean = false,
    val noExpertsMessage: String = "",
    val matchedExpertDate: String = "",
    val matchedExpertText: String = "",
    val matchedExpertId: String = "",
    val activeChatRoomId: String = "",
    val myRole: String = "",
    val activeChatQuestionText: String = "",
    val dailyRemainingQuota: Int = 3,
    val quotaError: String? = null
)

class SeekerViewModel(
    private val firebaseDb: FirebaseDatabase,
    private val prefs: SharedPreferences,
    private val matchingRepository: MatchingRepositoryInterface,
    private val aiRepository: AiRepository,
    private val mediaUploader: MediaUploader,
    private val questionRepository: QuestionRepository,
    private val validateQuestionQuotaUseCase: ValidateQuestionQuotaUseCase,
    private val observeQuestionStatusUseCase: ObserveQuestionStatusUseCase,
    private val sendQuestionMediaUseCase: SendQuestionMediaUseCase
) : ViewModel() {

    private val matchCoordinator = MatchCoordinator(firebaseDb, matchingRepository, aiRepository).apply {
        onAiChatroomReady = { _, _ -> }
    }

    private val _uiState = MutableStateFlow(SeekerUiState())
    val uiState: StateFlow<SeekerUiState> = _uiState.asStateFlow()

    val activeChatRoomId: String get() = _uiState.value.activeChatRoomId
    val matchedExpertId: String get() = _uiState.value.matchedExpertId
    val matchedExpertText: String get() = _uiState.value.matchedExpertText
    val matchedExpertDate: String get() = _uiState.value.matchedExpertDate

    private var currentUserQuestionId = ""
    private var currentUserId = ""
    private var statusCollectionJob: kotlinx.coroutines.Job? = null
    private var aiResponseJob: kotlinx.coroutines.Job? = null

    fun refreshQuota(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val count = questionRepository.getTodayQuestionCount(userId)
            val remaining = (3 - count).coerceAtLeast(0)
            _uiState.update { it.copy(dailyRemainingQuota = remaining) }
        }
    }

    fun clearQuotaError() {
        _uiState.update { it.copy(quotaError = null) }
    }

    fun clearActiveChatRoomId() {
        _uiState.update { it.copy(activeChatRoomId = "") }
    }

    fun sendQuestion(text: String, userId: String, selectedMedia: List<com.example.myapplication.domain.seeker.SendMedia> = emptyList()) {
        currentUserId = userId
        viewModelScope.launch {
            when (val result = validateQuestionQuotaUseCase(userId)) {
                is QuotaResult.Invalid -> {
                    _uiState.update { it.copy(quotaError = result.reason) }
                    return@launch
                }
                is QuotaResult.Valid -> { /* proceed */ }
            }

            questionRepository.sendQuestion(text, userId, object : QuestionRepository.SendQuestionCallback {
                override fun onSent(questionId: String) {
                    currentUserQuestionId = questionId
                    val chatroomId = "ai_$questionId"
                    val chatroomRef = firebaseDb.getReference("chatrooms").child(chatroomId)
                    val messagesRef = chatroomRef.child("messages")
                    val timestamp = System.currentTimeMillis()
                    val userMsgId = messagesRef.push().key ?: return

                    val userMessage = mapOf(
                        "senderId" to "",
                        "sender" to "user",
                        "text" to text,
                        "timestamp" to timestamp,
                        "readBy" to mapOf("system" to true)
                    )
                    chatroomRef.child("status").setValue("active")
                    messagesRef.child(userMsgId).setValue(userMessage)

                    _uiState.update { it.copy(
                        myRole = "user",
                        activeChatQuestionText = text,
                        isUserMatching = true
                    ) }

                    refreshQuota(userId)

                    matchCoordinator.startAiPreview(questionId, text, viewModelScope)
                    matchCoordinator.startMatchTimeout(questionId, viewModelScope)
                    matchCoordinator.matchAndAssignExpert(questionId, text, userId)
                    observeStatus(questionId)

                    aiResponseJob = viewModelScope.launch {
                        try {
                            val answer = aiRepository.generateResponse(text)
                            val aiMsgId = messagesRef.push().key ?: return@launch
                            val aiMessage = mapOf(
                                "senderId" to "ai_assistant",
                                "sender" to "ai",
                                "text" to answer,
                                "timestamp" to timestamp + 1,
                                "readBy" to mapOf("system" to true)
                            )
                            messagesRef.child(aiMsgId).setValue(aiMessage)

                            if (selectedMedia.isNotEmpty()) {
                                sendQuestionMediaUseCase(chatroomId, messagesRef, selectedMedia, timestamp + 2)
                            }
                        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                            Log.w("SeekerViewModel", "AI response failed", e)
                        }
                    }
                }

                override fun onError(message: String) {
                    Log.w("SeekerViewModel", "sendQuestion error: $message")
                }
            })
        }
    }

    fun observeStatus(questionId: String) {
        statusCollectionJob?.cancel()
        statusCollectionJob = viewModelScope.launch {
            observeQuestionStatusUseCase(questionId).collectLatest { status ->
                when (status) {
                    is QuestionStatus.Taken -> {
                        val chatroomId = "ai_$questionId"
                        val dateStr = if (status.timestamp > 0) {
                            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(status.timestamp))
                        } else ""
                        _uiState.update { it.copy(
                            activeChatRoomId = chatroomId,
                            myRole = "user",
                            activeChatQuestionText = status.questionText,
                            matchedExpertId = status.expertId,
                            matchedExpertText = status.expertText,
                            matchedExpertDate = dateStr,
                            isUserMatching = false
                        ) }
                        cleanupListeners()
                        prefs.edit().putString("lastQuestionId", questionId).apply()
                    }
                    is QuestionStatus.NoExperts -> {
                        _uiState.update { it.copy(isUserMatching = false) }
                        cleanupListeners()
                    }
                    is QuestionStatus.Cancelled -> {
                        _uiState.update { it.copy(isUserMatching = false) }
                        cleanupListeners()
                        refreshQuota(status.authorId)
                    }
                    is QuestionStatus.PendingAcceptance -> {
                        _uiState.update { it.copy(isPendingAcceptance = true) }
                    }
                    is QuestionStatus.Matching -> {
                        _uiState.update { it.copy(isPendingAcceptance = false) }
                    }
                }
            }
        }
    }

    fun resetToLoggedOutState() {
        _uiState.update { SeekerUiState() }
        currentUserQuestionId = ""
        cleanupListeners()
    }

    fun cancelUserMatching() {
        val uid = currentUserId
        val qId = currentUserQuestionId
        if (qId.isNotBlank()) {
            cleanupListeners()
            questionRepository.cancelMatching(qId) {
                resetMatchingState()
                if (uid.isNotBlank()) refreshQuota(uid)
            }
        } else {
            cleanupListeners()
            resetMatchingState()
        }
    }

    fun checkReconnection(userId: String) {
        questionRepository.checkReconnection(userId, object : QuestionRepository.ReconnectionListener {
            override fun onExpertChatActive(chatId: String, questionText: String) {
                val chatroomId = "ai_$chatId"
                _uiState.update { it.copy(
                    activeChatRoomId = chatroomId,
                    myRole = "user",
                    activeChatQuestionText = questionText
                ) }
            }

            override fun onUserReconnected(questionId: String, status: String, questionText: String) {
                if (questionId.isNotBlank() && status == "taken") {
                    val chatroomId = "ai_$questionId"
                    _uiState.update { it.copy(
                        activeChatRoomId = chatroomId,
                        myRole = "user",
                        activeChatQuestionText = questionText
                    ) }
                } else {
                    checkUserReconnection()
                }
            }
        })
    }

    fun submitRating(
        expertId: String,
        score: Double,
        comment: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        questionRepository.submitRating(
            expertId = expertId,
            score = score,
            comment = comment,
            activeChatRoomId = _uiState.value.activeChatRoomId,
            callback = object : QuestionRepository.RatingCallback {
                override fun onSuccess() { onSuccess() }
                override fun onError(message: String) { onError(message) }
            }
        )
    }

    private fun resetMatchingState() {
        _uiState.update { it.copy(isUserMatching = false, isPendingAcceptance = false, noExpertsMessage = "") }
        currentUserQuestionId = ""
    }

    private fun cleanupListeners() {
        matchCoordinator.cancelMatchTimeout()
        statusCollectionJob?.cancel()
        statusCollectionJob = null
        aiResponseJob?.cancel()
        aiResponseJob = null
    }

    private fun checkUserReconnection() {
        val lastQuestionId = prefs.getString("lastQuestionId", "") ?: ""
        if (lastQuestionId.isBlank()) return
        val ref = firebaseDb.getReference(FirebasePaths.QUESTIONS).child(lastQuestionId)
        ref.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").value?.toString()
                if (status == StatusValues.TAKEN) {
                    val chatroomId = "ai_$lastQuestionId"
                    _uiState.update { it.copy(
                        activeChatRoomId = chatroomId,
                        myRole = "user",
                        activeChatQuestionText = snapshot.child("text").value?.toString().orEmpty()
                    ) }
                } else if (status == StatusValues.PENDING_ACCEPTANCE) {
                    _uiState.update { it.copy(isPendingAcceptance = true) }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.w("SeekerViewModel", "checkUserReconnection cancelled", error.toException())
            }
        })
    }

    override fun onCleared() {
        cleanupListeners()
        matchCoordinator.cleanup()
        super.onCleared()
    }
}
