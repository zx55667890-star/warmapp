package com.example.myapplication.di

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
    val activeExperienceId: String = "",
    val activeExperienceText: String = "",

    val isEditing: Boolean = false,
    val editText: String = "",
    @StringRes val editErrorRes: Int? = null,
    val isSubmitting: Boolean = false,

    val skillEditTarget: SolutionItem? = null,

    val showGlobalAssignDialog: Boolean = false,
    val isExpertWaitingForSeeker: Boolean = false,
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
) : ViewModel() {
    private val repository = ExpertRepository(firebaseDb)

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
                repository.listenToSolutionHistory(userId).collect { history ->
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
                it.expertise == trimmed && it.status != SkillStatus.REJECTED
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
                repository.saveSkill(userId, trimmed)
                _uiState.update { it.copy(
                    publishFeedbackRes = R.string.expert_toast_skill_submitted,
                    publishFeedbackIsError = false
                ) }
                sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_skill_submitted))
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
                repository.observeExpertStatus(userId).collect { (rating, helpCount) ->
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
        repository.setExpertOnline(online, userId, _uiState.value.activeExperienceId)
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

    fun publishExperience(userId: String, text: String) {
        if (userId.isBlank()) {
            sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_login_required))
            return
        }
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_experience_blank))
            return
        }
        viewModelScope.launch {
            try {
                val experienceId = repository.publishExperience(userId, trimmed)
                _uiState.update { it.copy(activeExperienceId = experienceId, activeExperienceText = trimmed) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                sendEvent(ExpertUiEvent.ShowToast(R.string.expert_toast_publish_failed))
            }
        }
    }

    fun startEditing() {
        _uiState.update {
            it.copy(isEditing = true, editText = it.activeExperienceText, editErrorRes = null)
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, editErrorRes = null) }
    }

    fun updateEditText(newText: String) {
        _uiState.update { it.copy(editText = newText, editErrorRes = null) }
    }

    fun submitEdit() {
        val currentState = _uiState.value
        val trimmed = currentState.editText.trim()

        if (trimmed.isBlank()) {
            _uiState.update { it.copy(editErrorRes = R.string.expert_error_experience_blank) }
            return
        }
        if (trimmed.length > 200) {
            _uiState.update { it.copy(editErrorRes = R.string.expert_error_experience_too_long) }
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
                        editErrorRes = null
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                sendEvent(ExpertUiEvent.ShowToastRaw(e.message ?: "更新失敗"))
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun stopExperience() {
        repository.stopExperience(_uiState.value.activeExperienceId)
        _uiState.update {
            it.copy(activeExperienceId = "", activeExperienceText = "", isEditing = false)
        }
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
                repository.editSkill(userId, target.id, trimmed)
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
        cleanupGlobalListener()
        val query = firebaseDb.getReference(FirebasePaths.QUESTIONS)
            .orderByChild(FirebaseFields.EXPERT_ID)
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
                        StatusValues.PENDING_ACCEPTANCE -> {
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
                        StatusValues.EXPERT_ACCEPTED -> {
                            if (qId == currentState.globalAssignedQId || currentState.globalAssignedQId.isBlank()) {
                                _uiState.update { it.copy(isExpertWaitingForSeeker = true, showGlobalAssignDialog = false) }
                                foundActiveAssignment = true
                                return
                            }
                        }
                        StatusValues.TAKEN -> {
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
                    if (currentStatus == null || currentStatus == StatusValues.MATCHING || currentStatus == StatusValues.CANCELLED) {
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
            firebaseDb.getReference(FirebasePaths.QUESTIONS).child(qId).child("status").setValue(StatusValues.EXPERT_ACCEPTED)
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
            val qRef = firebaseDb.getReference(FirebasePaths.QUESTIONS).child(qId)
            qRef.child("rejectedExperts").child(userId).setValue(true)
            qRef.child("status").setValue(StatusValues.MATCHING)
        }
        _uiState.update { it.copy(showGlobalAssignDialog = false) }
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
        repository.cleanup(_uiState.value.activeExperienceId)
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
