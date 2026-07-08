package com.example.myapplication.di

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.domain.seeker.MatchCoordinator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SeekerUiState(
    val isUserMatching: Boolean = false,
    val noExpertsMessage: String = "",
    val showSeekerConfirmDialog: Boolean = false,
    val matchedExpertDate: String = "",
    val matchedExpertText: String = "",
    val matchedExpertId: String = "",
    val activeChatRoomId: String = "",
    val myRole: String = "",
    val activeChatQuestionText: String = "",
)

class SeekerViewModel(
    private val firebaseDb: FirebaseDatabase,
    private val prefs: SharedPreferences,
    private val matchingRepository: MatchingRepositoryInterface,
    private val aiRepository: AiRepository,
    private val mediaUploader: MediaUploader,
    private val questionRepository: QuestionRepository = QuestionRepository(firebaseDb)
) : ViewModel() {

    private val matchCoordinator = MatchCoordinator(firebaseDb, matchingRepository, aiRepository).apply {
        onAiChatroomReady = { chatroomId, questionText ->
            _uiState.update { it.copy(
                activeChatRoomId = chatroomId,
                myRole = "user",
                activeChatQuestionText = questionText
            ) }
        }
        onCancelUserMatching = { cancelUserMatching() }
    }

    val showSeekerConfirmDialog: Boolean
        get() = _uiState.value.showSeekerConfirmDialog
    private val _uiState = MutableStateFlow(SeekerUiState())
    val uiState: StateFlow<SeekerUiState> = _uiState.asStateFlow()

    val activeChatRoomId: String get() = _uiState.value.activeChatRoomId
    val matchedExpertId: String get() = _uiState.value.matchedExpertId
    val matchedExpertText: String get() = _uiState.value.matchedExpertText
    val matchedExpertDate: String get() = _uiState.value.matchedExpertDate

    private var currentUserQuestionId = ""
    private var userQuestionRef: DatabaseReference? = null
    private var userQuestionListener: ValueEventListener? = null

    fun sendQuestion(text: String, userId: String, selectedMedia: List<SendMedia> = emptyList()) {
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
                    activeChatRoomId = chatroomId,
                    myRole = "user",
                    activeChatQuestionText = text,
                    isUserMatching = true
                ) }
                matchCoordinator.matchAndAssignExpert(questionId, text, userId)
                matchCoordinator.startAiPreview(questionId, text, viewModelScope)
                matchCoordinator.startMatchTimeout(questionId, viewModelScope)
                listenToMyQuestionStatus(questionId)

                viewModelScope.launch {
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
                            uploadSelectedMedia(chatroomId, messagesRef, selectedMedia, timestamp + 2)
                        }
                    } catch (e: Exception) {
                        Log.w("SeekerViewModel", "AI response failed", e)
                    }
                }
            }

            override fun onError(message: String) {
                Log.w("SeekerViewModel", "sendQuestion error: $message")
            }
        })
    }

    data class SendMedia(val uri: Uri, val isVideo: Boolean, val isVoice: Boolean)

    private suspend fun uploadSelectedMedia(
        chatroomId: String,
        messagesRef: DatabaseReference,
        mediaList: List<SendMedia>,
        startTimestamp: Long
    ) {
        withContext(Dispatchers.IO) {
            mediaList.forEachIndexed { index, media ->
                try {
                    val timestamp = startTimestamp + index
                    if (media.isVoice) {
                        val voicePath = media.uri.path ?: return@forEachIndexed
                        val voiceUrl = mediaUploader.sendVoice(
                            chatroomId = chatroomId,
                            filePath = voicePath,
                            onProgress = {},
                            onError = { Log.w("SeekerViewModel", "Voice upload failed: $it") }
                        )
                        if (voiceUrl != null) {
                            val msgId = messagesRef.push().key ?: return@forEachIndexed
                            val msg = mapOf(
                                "senderId" to "",
                                "sender" to "user",
                                "text" to "",
                                "timestamp" to timestamp,
                                "voiceUrl" to voiceUrl,
                                "readBy" to mapOf("system" to true)
                            )
                            messagesRef.child(msgId).setValue(msg)
                        }
                    } else if (media.isVideo) {
                        val videoUrl = mediaUploader.sendVideo(
                            chatroomId = chatroomId,
                            uri = media.uri,
                            onProgress = {},
                            onError = { Log.w("SeekerViewModel", "Video upload failed: $it") }
                        )
                        if (videoUrl != null) {
                            val msgId = messagesRef.push().key ?: return@forEachIndexed
                            val msg = mapOf(
                                "senderId" to "",
                                "sender" to "user",
                                "text" to "",
                                "timestamp" to timestamp,
                                "videoUrl" to videoUrl,
                                "readBy" to mapOf("system" to true)
                            )
                            messagesRef.child(msgId).setValue(msg)
                        }
                    } else {
                        val urls = mediaUploader.sendImages(
                            chatroomId = chatroomId,
                            userId = "",
                            myRole = "user",
                            uris = listOf(media.uri),
                            onProgress = {},
                            onError = { Log.w("SeekerViewModel", "Image upload failed: $it") }
                        )
                        if (urls.isNotEmpty()) {
                            val msgId = messagesRef.push().key ?: return@forEachIndexed
                            val msg = mapOf(
                                "senderId" to "",
                                "sender" to "user",
                                "text" to "",
                                "timestamp" to timestamp,
                                "imageUrls" to urls,
                                "readBy" to mapOf("system" to true)
                            )
                            messagesRef.child(msgId).setValue(msg)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("SeekerViewModel", "Media upload failed", e)
                }
            }
        }
    }

    fun listenToMyQuestionStatus(questionId: String) {
        removeUserQuestionListener()
        val ref = firebaseDb.getReference("questions").child(questionId)
        userQuestionRef = ref
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").value?.toString() ?: return
                when (status) {
                    "taken" -> {
                        _uiState.update { it.copy(
                            activeChatRoomId = questionId,
                            myRole = "user",
                            activeChatQuestionText = snapshot.child("text").value?.toString().orEmpty(),
                            isUserMatching = false,
                            showSeekerConfirmDialog = false
                        ) }
                        cleanupListeners()
                        prefs.edit().putString("lastQuestionId", questionId).apply()
                    }
                    "expert_accepted" -> {
                        val expertId = snapshot.child("expertId").value?.toString().orEmpty()
                        val expertText = snapshot.child("matchedExpText").value?.toString().orEmpty()
                        val timestamp = snapshot.child("matchedExpTimestamp").value as? Long ?: 0L
                        val dateStr = if (timestamp > 0) {
                            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
                        } else ""
                        _uiState.update { it.copy(
                            matchedExpertId = expertId,
                            matchedExpertText = expertText,
                            matchedExpertDate = dateStr,
                            showSeekerConfirmDialog = true,
                            isUserMatching = false
                        ) }
                        cleanupListeners()
                    }
                    "no_experts" -> {
                        _uiState.update { it.copy(isUserMatching = false) }
                        cleanupListeners()
                    }
                    "cancelled" -> {
                        _uiState.update { it.copy(isUserMatching = false, showSeekerConfirmDialog = false) }
                        cleanupListeners()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("SeekerViewModel", "Question status listener cancelled", error.toException())
            }
        }
        ref.addValueEventListener(listener)
        userQuestionListener = listener
    }

    fun acceptExpertMatch() {
        if (currentUserQuestionId.isBlank()) return
        questionRepository.acceptExpertMatch(currentUserQuestionId, onSuccess = {
            _uiState.update { it.copy(showSeekerConfirmDialog = false) }
        }, onError = { msg ->
            Log.w("SeekerViewModel", "acceptExpertMatch error: $msg")
        })
    }

    fun rejectExpertMatch() {
        if (currentUserQuestionId.isBlank()) return
        val expertId = _uiState.value.matchedExpertId
        if (expertId.isNotBlank()) {
            questionRepository.rejectExpertMatch(currentUserQuestionId, expertId,
                onSuccess = { _uiState.update { it.copy(showSeekerConfirmDialog = false) } },
                onError = { _uiState.update { it.copy(showSeekerConfirmDialog = false) } }
            )
        } else {
            _uiState.update { it.copy(showSeekerConfirmDialog = false) }
        }
    }

    fun resetToLoggedOutState() {
        _uiState.update { SeekerUiState() }
        currentUserQuestionId = ""
        cleanupListeners()
    }

    fun cancelUserMatching() {
        cleanupListeners()
        if (currentUserQuestionId.isNotBlank()) {
            questionRepository.cancelMatching(currentUserQuestionId) {
                resetMatchingState()
            }
        } else {
            resetMatchingState()
        }
    }

    fun checkReconnection(userId: String) {
        questionRepository.checkReconnection(userId, object : QuestionRepository.ReconnectionListener {
            override fun onExpertChatActive(chatId: String, questionText: String) {
                _uiState.update { it.copy(
                    activeChatRoomId = chatId,
                    myRole = "user",
                    activeChatQuestionText = questionText
                ) }
            }

            override fun onUserReconnected(questionId: String, status: String, questionText: String) {
                if (questionId.isNotBlank() && status == "taken") {
                    _uiState.update { it.copy(
                        activeChatRoomId = questionId,
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

    // -- private helpers --

    private fun resetMatchingState() {
        _uiState.update { it.copy(isUserMatching = false, noExpertsMessage = "") }
        currentUserQuestionId = ""
    }

    private fun removeUserQuestionListener() {
        userQuestionRef?.let { ref ->
            userQuestionListener?.let { ref.removeEventListener(it) }
        }
        userQuestionRef = null
        userQuestionListener = null
    }

    private fun cleanupListeners() {
        matchCoordinator.cancelMatchTimeout()
        removeUserQuestionListener()
    }

    private fun checkUserReconnection() {
        val lastQuestionId = prefs.getString("lastQuestionId", "") ?: ""
        if (lastQuestionId.isBlank()) return
        val ref = firebaseDb.getReference("questions").child(lastQuestionId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                val status = snapshot.child("status").value?.toString()
                if (status == "taken") {
                    _uiState.update { it.copy(
                        activeChatRoomId = lastQuestionId,
                        myRole = "user",
                        activeChatQuestionText = snapshot.child("text").value?.toString().orEmpty()
                    ) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
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
