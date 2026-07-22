package com.example.myapplication.ui.expert

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.FirebaseFields
import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.StatusValues
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.data.repository.ExpertRepository
import com.example.myapplication.domain.expert.ExpertInputValidator
import com.example.myapplication.domain.expert.ObserveSolutionsUseCase
import com.example.myapplication.domain.expert.PublishSkillUseCase
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
    val solutionHistory: List<SolutionItem> = emptyList(),
    val isSubmittingSolution: Boolean = false,
    val editText: String = "",
    @StringRes val editErrorRes: Int? = null,
    val isSubmitting: Boolean = false,
    val skillEditTarget: SolutionItem? = null,
    val showGlobalAssignDialog: Boolean = false,
    val globalAssignedQId: String = "",
    val globalAssignedQText: String = "",
    val activeChatRoomId: String = "",
    val activeChatQuestionText: String = "",
    val myRole: String = "",
    val isSubmissionLocked: Boolean = false,
    @StringRes val publishFeedbackRes: Int? = null,
    val publishFeedbackIsError: Boolean = false
)
sealed class ExpertUiEvent {
    data class ShowToast(@StringRes val resId: Int) : ExpertUiEvent()
    data class ShowToastRaw(val message: String) : ExpertUiEvent()
}

class ExpertViewModel(
    private val firebaseDb: FirebaseDatabase,
    private val expertRepository: ExpertRepository,
    private val publishSkillUseCase: PublishSkillUseCase,
    private val observeSolutionsUseCase: ObserveSolutionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpertUiState())
    val uiState: StateFlow<ExpertUiState> = _uiState.asStateFlow()


    private val _uiEvent = Channel<ExpertUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var globalQuery: Query? = null
    private var globalListener: ValueEventListener? = null
    private var lockRef: com.google.firebase.database.DatabaseReference? = null
    private var lockListener: ValueEventListener? = null

    fun listenToSolutions(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            try {
                observeSolutionsUseCase(userId).collect { history ->
                    _uiState.update { it.copy(solutionHistory = history) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(solutionHistory = emptyList()) }
            }
        }
    }

    fun publishSkill(userId: String, text: String) {
        if (userId.isBlank()) {
            sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_login_required))
            return
        }
        if (_uiState.value.isSubmissionLocked) {
            sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_submission_locked))
            return
        }
        viewModelScope.launch {
            val trimmed = text.trim()

            val isDuplicate = _uiState.value.solutionHistory.any {
                if (it.status == SkillStatus.REJECTED) false else {
                    it.expertise == trimmed || computeTextSimilarity(it.expertise, trimmed) >= 0.7
                }
            }
            if (isDuplicate) {
                sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_already_exists))
                return@launch
            }

            val validationError = ExpertInputValidator.validate(trimmed)
            if (validationError != null) {
                _uiState.update { it.copy(
                    publishFeedbackRes = validationError.toResourceId(),
                    publishFeedbackIsError = true
                ) }
                return@launch
            }

            try {
                publishSkillUseCase(userId, trimmed)
                _uiState.update { it.copy(
                    publishFeedbackRes = R.string.expert_toast_skill_submitted,
                    publishFeedbackIsError = false
                ) }
                
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("ExpertVM", "saveSkill failed", e)
                _uiState.update { it.copy(publishFeedbackRes = null, publishFeedbackIsError = true) }
                sendEvent(ExpertUiEvent.ShowToastRaw("記錄失敗：${e.javaClass.simpleName}: ${e.message}"))
            }
        }
    }

    fun clearPublishFeedback() {
        _uiState.update { it.copy(publishFeedbackRes = null, publishFeedbackIsError = false) }
    }

    fun initializeExpertStatus(userId: String) {
        if (userId.isBlank()) {
            _uiState.update { it.copy(rating = 5.0, helpCount = 0L, solutionHistory = emptyList()) }
            return
        }
        viewModelScope.launch {
            try {
                expertRepository.observeExpertStatus(userId).collect { (rating, helpCount) ->
                    _uiState.update { it.copy(rating = rating, helpCount = helpCount) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(rating = 5.0, helpCount = 0L) }
            }
        }
        listenToSolutions(userId)
        observeSubmissionLock(userId)
    }

    fun setExpertOnline(online: Boolean, userId: String) {
        expertRepository.setExpertOnline(online, userId, "")
    }

    private fun observeSubmissionLock(userId: String) {
        lockListener?.let { lockRef?.removeEventListener(it) }
        if (userId.isBlank()) return
        lockRef = firebaseDb.getReference(FirebasePaths.USERS).child(userId).child("submissionLock")
        lockListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lockedUntil = snapshot.child("lockedUntil").getValue(Long::class.java) ?: 0L
                _uiState.update { it.copy(isSubmissionLocked = lockedUntil > System.currentTimeMillis()) }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        lockRef?.addValueEventListener(lockListener!!)
    }

    fun startSkillEdit(solution: SolutionItem) {
        _uiState.update {
            it.copy(
                skillEditTarget = solution,
                editText = solution.expertise,
                editErrorRes = null
            )
        }
    }

    fun cancelSkillEdit() {
        _uiState.update { it.copy(skillEditTarget = null, editText = "", editErrorRes = null) }
    }

    fun updateSkillEditText(newText: String) {
        _uiState.update { it.copy(editText = newText, editErrorRes = null) }
    }

    fun submitSkillEdit(userId: String) {
        val currentState = _uiState.value
        val target = currentState.skillEditTarget ?: return
        val trimmed = currentState.editText.trim()

        if (trimmed.isBlank()) {
            _uiState.update { it.copy(editErrorRes = R.string.expert_error_skill_blank) }
            return
        }
        if (trimmed.length < ExpertInputValidator.MIN_SKILL_LENGTH) {
            _uiState.update { it.copy(editErrorRes = R.string.expert_error_skill_too_short) }
            return
        }
        if (trimmed == target.expertise) {
            _uiState.update { it.copy(skillEditTarget = null, editText = "", editErrorRes = null) }
            return
        }

        val validationError = ExpertInputValidator.validate(trimmed)
        if (validationError != null) {
            _uiState.update { it.copy(editErrorRes = validationError.toResourceId()) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                expertRepository.editSkill(userId, target.id, trimmed)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        skillEditTarget = null,
                        editText = "",
                        editErrorRes = null
                    )
                }
                sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_skill_updated))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                sendEvent(ExpertUiEvent.ShowToastRaw(e.message ?: "更新失敗"))
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun startGlobalAssignListener(userId: String) {
        Log.d("ExpertVM", "startGlobalAssignListener userId=$userId")
        cleanupGlobalListener()
        val query = firebaseDb.getReference(FirebasePaths.QUESTIONS)
            .orderByChild(FirebaseFields.EXPERT_ID)
            .equalTo(userId)
        globalQuery = query

        globalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ExpertVM", "onDataChange children=${snapshot.children.count()}")
                var foundActiveAssignment = false
                val currentState = _uiState.value
                Log.d("ExpertVM", "  current globalAssignedQId=[${currentState.globalAssignedQId}] showDialog=[${currentState.showGlobalAssignDialog}]")

                for (child in snapshot.children) {
                    val status = child.child("status").value?.toString()
                    val qId = child.key.orEmpty()
                    val eId = child.child("expertId").value?.toString()
                    Log.d("ExpertVM", "  child qId=$qId status=$status expertId=$eId")

                    when (status) {
                        StatusValues.PENDING_ACCEPTANCE -> {
                            if (qId == currentState.globalAssignedQId) {
                                Log.d("ExpertVM", ">>> navigate to chat for qId=$qId")
                                val chatroomId = "ai_$qId"
                                _uiState.update {
                                    it.copy(
                                        activeChatRoomId = chatroomId,
                                        myRole = "expert",
                                        activeChatQuestionText = child.child("text").value?.toString().orEmpty(),
                                        showGlobalAssignDialog = false
                                    )
                                }
                                foundActiveAssignment = true
                                return
                            } else if (currentState.globalAssignedQId.isBlank()) {
                                Log.d("ExpertVM", ">>> show dialog for qId=$qId")
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
                        }
                    }
                }

                if (!foundActiveAssignment && currentState.globalAssignedQId.isNotBlank()) {
                    val currentChild = snapshot.children.firstOrNull { it.key == currentState.globalAssignedQId }
                    val currentStatus = currentChild?.child("status")?.value?.toString()
                    Log.d("ExpertVM", "  cleanup check: currentStatus=$currentStatus")
                    if (currentStatus == null || currentStatus == StatusValues.MATCHING || currentStatus == StatusValues.CANCELLED) {
                        Log.d("ExpertVM", ">>> cleanup: clearing assignment")
                        _uiState.update {
                            it.copy(
                                globalAssignedQId = "",
                                globalAssignedQText = "",
                                showGlobalAssignDialog = false
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("ExpertVM", "CANCELLED code=${error.code} msg=${error.message}", error.toException())
            }
        }
        query.addValueEventListener(globalListener!!)
    }

    fun acceptGlobalAssignment() {
        val qId = _uiState.value.globalAssignedQId
        if (qId.isNotBlank()) {
            firebaseDb.getReference(FirebasePaths.QUESTIONS).child(qId).child(FirebaseFields.STATUS)
                .setValue(StatusValues.TAKEN)
            val chatroomId = "ai_$qId"
            _uiState.update {
                it.copy(
                    showGlobalAssignDialog = false,
                    activeChatRoomId = chatroomId,
                    myRole = "expert"
                )
            }
        } else {
            _uiState.update { it.copy(showGlobalAssignDialog = false) }
        }
    }

    fun rejectGlobalAssignment(userId: String) {
        handleRejection(userId)
    }

    private fun handleRejection(userId: String) {
        val qId = _uiState.value.globalAssignedQId
        if (qId.isNotBlank()) {
            val qRef = firebaseDb.getReference(FirebasePaths.QUESTIONS).child(qId)
            qRef.updateChildren(mapOf(
                "rejectedExperts/$userId" to true,
                "status" to StatusValues.MATCHING
            ))
        }
        _uiState.update { 
            it.copy(
                showGlobalAssignDialog = false,
                globalAssignedQId = "",
                globalAssignedQText = ""
            ) 
        }
    }

    private fun computeTextSimilarity(a: String, b: String): Double {
        val bigramsA = a.windowed(2).toSet()
        val bigramsB = b.windowed(2).toSet()
        val intersect = bigramsA.intersect(bigramsB).size.toDouble()
        val union = bigramsA.union(bigramsB).size.toDouble()
        return if (union > 0) intersect / union else 0.0
    }

    private fun ExpertInputValidator.ValidationError.toResourceId(): Int = when (this) {
        ExpertInputValidator.ValidationError.BLANK -> R.string.expert_input_blank
        ExpertInputValidator.ValidationError.TOO_SHORT -> R.string.expert_input_too_short
        ExpertInputValidator.ValidationError.NO_MEANINGFUL_CHAR -> R.string.expert_input_no_meaningful_char
        ExpertInputValidator.ValidationError.HIGH_REPETITION -> R.string.expert_input_high_repetition
        ExpertInputValidator.ValidationError.GIBBERISH -> R.string.expert_input_gibberish
        ExpertInputValidator.ValidationError.INVALID_ENGLISH -> R.string.expert_input_invalid_english
    }

    private fun sendEvent(event: ExpertUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    fun resetToLoggedOutState() {
        cleanup()
        _uiState.value = ExpertUiState()
    }

    private fun cleanupGlobalListener() {
        globalListener?.let { globalQuery?.removeEventListener(it) }
        globalQuery = null
        globalListener = null
    }

    fun cleanup() {
        cleanupGlobalListener()
        cleanupLockListener()
    }

    private fun cleanupLockListener() {
        lockListener?.let { lockRef?.removeEventListener(it) }
        lockRef = null
        lockListener = null
    }

    override fun onCleared() {
        cleanup()
        super.onCleared()
    }
}
