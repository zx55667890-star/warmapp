package com.example.myapplication.di

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.data.repository.ExpertRepository
import com.example.myapplication.domain.expert.ExtractLocalTagsUseCase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpertUiState(
    val rating: Double = 5.0,
    val helpCount: Long = 0L,
    // 新增：知識庫狀態
    val solutionHistory: List<SolutionItem> = emptyList(),
    val isSubmittingSolution: Boolean = false,
    val activeExperienceId: String = "",
    val activeExperienceText: String = "",

    val isEditing: Boolean = false,
    val editText: String = "",
    val editError: String? = null,
    val isSubmitting: Boolean = false,

    val showGlobalAssignDialog: Boolean = false,
    val isExpertWaitingForSeeker: Boolean = false,
    val globalAssignedQId: String = "",
    val globalAssignedQText: String = "",
    val activeChatRoomId: String = "",
    val activeChatQuestionText: String = "",
    val myRole: String = ""
)
sealed class ExpertUiEvent {
    data class ShowToast(val message: String) : ExpertUiEvent()
}

class ExpertViewModel(
    private val firebaseDb: FirebaseDatabase,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {
    private val repository = ExpertRepository(firebaseDb)
    private val extractLocalTagsUseCase = ExtractLocalTagsUseCase(sharedPrefs, firebaseDb)

    private val _uiState = MutableStateFlow(ExpertUiState())
    val uiState: StateFlow<ExpertUiState> = _uiState.asStateFlow()


    private val _uiEvent = Channel<ExpertUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var globalQuery: Query? = null
    private var globalListener: ValueEventListener? = null

    fun listenToSolutions(userId: String) {
        repository.listenToSolutionHistory(userId) { history ->
            _uiState.update { it.copy(solutionHistory = history) }
        }
    }

    fun submitSolution(userId: String, questionId: String, expertise: String, tags: List<String>) {
        _uiState.update { it.copy(isSubmittingSolution = true) }
        viewModelScope.launch {
            try {
                repository.saveSolution(userId, questionId, expertise, tags)
                _uiState.update { it.copy(isSubmittingSolution = false) }
                sendEvent(ExpertUiEvent.ShowToast("已成功記錄到您的知識庫！"))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isSubmittingSolution = false) }
                sendEvent(ExpertUiEvent.ShowToast("記錄失敗：${e.message}"))
            }
        }
    }

    fun fetchTagsFromAi(text: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            Log.d("ExpertVM", "fetchTagsFromAi: text=$text")
            try {
                val tags = extractLocalTagsUseCase(text)
                onResult(tags)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("ExpertVM", "AI 標籤擷取失敗: ${e.message}")
                sendEvent(ExpertUiEvent.ShowToast(e.message ?: "AI 標籤擷取失敗"))
                onResult(emptyList())
            }
        }
    }

    fun initializeExpertStatus(userId: String) {
        repository.initializeExpertStatus(userId) { rating, helpCount ->
            _uiState.update { it.copy(rating = rating, helpCount = helpCount) }
        }
        listenToSolutions(userId)
    }

    fun setExpertOnline(online: Boolean) {
        repository.setExpertOnline(online, _uiState.value.activeExperienceId)
    }

    fun publishExperience(userId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            sendEvent(ExpertUiEvent.ShowToast("請填寫您的經驗描述"))
            return
        }
        viewModelScope.launch {
            try {
                val experienceId = repository.publishExperience(userId, trimmed)
                _uiState.update { it.copy(activeExperienceId = experienceId, activeExperienceText = trimmed) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                sendEvent(ExpertUiEvent.ShowToast(e.message ?: "發佈失敗"))
            }
        }
    }

    fun startEditing() {
        _uiState.update {
            it.copy(isEditing = true, editText = it.activeExperienceText, editError = null)
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, editError = null) }
    }

    fun updateEditText(newText: String) {
        _uiState.update { it.copy(editText = newText, editError = null) }
    }

    fun submitEdit() {
        val currentState = _uiState.value
        val trimmed = currentState.editText.trim()

        if (trimmed.isBlank()) {
            _uiState.update { it.copy(editError = "經驗描述不能為空白") }
            return
        }
        if (trimmed.length > 200) {
            _uiState.update { it.copy(editError = "經驗描述不能超過 200 個字元（目前 ${trimmed.length} 字）") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                repository.editExperience(
                    experienceId = currentState.activeExperienceId,
                    newText = trimmed
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isEditing = false,
                        activeExperienceText = trimmed,
                        editError = null
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update { it.copy(isSubmitting = false, editError = e.message) }
            }
        }
    }

    fun stopExperience() {
        repository.stopExperience(_uiState.value.activeExperienceId)
        _uiState.update {
            it.copy(activeExperienceId = "", activeExperienceText = "", isEditing = false)
        }
    }

    fun startGlobalAssignListener(userId: String) {
        if (globalListener != null) return
        val query = firebaseDb.getReference("questions")
            .orderByChild("expertId")
            .equalTo(userId)
        globalQuery = query

        globalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var foundActiveAssignment = false
                val currentState = _uiState.value

                for (child in snapshot.children) {
                    val status = child.child("status").value?.toString()
                    val qId = child.key.orEmpty()

                    when (status) {
                        "pending_acceptance" -> {
                            _uiState.update {
                                it.copy(
                                    globalAssignedQId = qId,
                                    globalAssignedQText = child.child("text").value?.toString().orEmpty(),
                                    showGlobalAssignDialog = true
                                )
                            }
                            foundActiveAssignment = true
                            return
                        }
                        "expert_accepted" -> {
                            if (qId == currentState.globalAssignedQId || currentState.globalAssignedQId.isBlank()) {
                                _uiState.update { it.copy(isExpertWaitingForSeeker = true, showGlobalAssignDialog = false) }
                                foundActiveAssignment = true
                                return
                            }
                        }
                        "taken" -> {
                            if (qId == currentState.globalAssignedQId) {
                                _uiState.update {
                                    it.copy(
                                        activeChatRoomId = qId,
                                        myRole = "expert",
                                        activeChatQuestionText = child.child("text").value?.toString().orEmpty(),
                                        isExpertWaitingForSeeker = false
                                    )
                                }
                                foundActiveAssignment = true
                                return
                            }
                        }
                    }
                }

                if (!foundActiveAssignment && currentState.globalAssignedQId.isNotBlank()) {
                    val currentChild = snapshot.children.firstOrNull { it.key == currentState.globalAssignedQId }
                    val currentStatus = currentChild?.child("status")?.value?.toString()
                    if (currentStatus == null || currentStatus == "matching" || currentStatus == "cancelled") {
                        _uiState.update {
                            it.copy(
                                globalAssignedQId = "",
                                globalAssignedQText = "",
                                showGlobalAssignDialog = false,
                                isExpertWaitingForSeeker = false
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ExpertViewModel", "Global listener cancelled", error.toException())
            }
        }
        query.addValueEventListener(globalListener!!)
    }

    fun acceptGlobalAssignment() {
        val qId = _uiState.value.globalAssignedQId
        if (qId.isNotBlank()) {
            firebaseDb.getReference("questions").child(qId).child("status").setValue("expert_accepted")
        }
        _uiState.update { it.copy(showGlobalAssignDialog = false) }
    }

    fun rejectGlobalAssignment(userId: String) {
        handleRejection(userId)
    }

    fun cancelWaiting(userId: String) {
        handleRejection(userId)
        _uiState.update { it.copy(isExpertWaitingForSeeker = false) }
    }

    private fun handleRejection(userId: String) {
        val qId = _uiState.value.globalAssignedQId
        if (qId.isNotBlank()) {
            val qRef = firebaseDb.getReference("questions").child(qId)
            qRef.child("rejectedExperts").child(userId).setValue(true)
            qRef.child("status").setValue("matching")
        }
        _uiState.update { it.copy(showGlobalAssignDialog = false) }
    }

    private fun sendEvent(event: ExpertUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    fun resetToLoggedOutState() {
        cleanup()
        _uiState.value = ExpertUiState()
    }

    fun cleanup() {
        globalListener?.let { globalQuery?.removeEventListener(it) }
        globalQuery = null
        globalListener = null
        repository.cleanup(_uiState.value.activeExperienceId)
    }

    override fun onCleared() {
        cleanup()
        super.onCleared()
    }
}
