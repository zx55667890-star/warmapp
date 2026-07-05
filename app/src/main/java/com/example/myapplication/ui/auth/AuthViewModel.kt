package com.example.myapplication.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.common.AuthUtils
import com.example.myapplication.ui.common.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isRegisterMode: Boolean = false,
    val isLoggedIn: Boolean = false,
    val verificationSent: Boolean = false,
    val verificationSentTo: String = "",
    val verificationLastSentAt: Long = 0L,
    val resetSent: Boolean = false,
    val resetLastSentAt: Long = 0L,
    val resetVerificationLastSentAt: Long = 0L,
    val showNewPasswordForm: Boolean = false,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _toastEvent = Channel<UiText>(Channel.BUFFERED)
    val toastEvent = _toastEvent.receiveAsFlow()

    private val _navigateEvent = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigateEvent = _navigateEvent.receiveAsFlow()

    sealed class NavigationEvent {
        data object ShowLoginForm : NavigationEvent()
    }

    init {
        _uiState.update { it.copy(isLoggedIn = authRepository.isLoggedIn()) }
    }

    fun refreshLoggedInState() {
        _uiState.update { it.copy(isLoggedIn = authRepository.isLoggedIn()) }
    }

    fun sendVerificationCode(email: String) = sendVerificationInternal(email)
    fun sendResetVerificationCode(email: String) = sendVerificationInternal(email, prefix = "reset_")

    private fun sendVerificationInternal(email: String, prefix: String = "") {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請先輸入 Email")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("錯誤的信箱格式")) }
            return
        }
        val now = System.currentTimeMillis()
        val isReset = prefix == "reset_"
        val lastSent = if (isReset) _uiState.value.resetVerificationLastSentAt else _uiState.value.verificationLastSentAt
        if (now - lastSent < 60_000L) {
            val remain = 60 - (now - lastSent) / 1000
            _uiState.update { it.copy(error = UiText.Dynamic("請 ${remain} 秒後再試")) }
            return
        }
        viewModelScope.launch {
            try {
                authRepository.generateVerificationCode(email, prefix = prefix)
                _uiState.update {
                    it.copy(
                        error = null, verificationSent = true,
                        verificationSentTo = email,
                        verificationLastSentAt = if (!isReset) now else it.verificationLastSentAt,
                        resetVerificationLastSentAt = if (isReset) now else it.resetVerificationLastSentAt
                    )
                }
                _toastEvent.send(UiText.Dynamic("驗證碼已發送至 $email"))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = UiText.Dynamic(e.message ?: "發送失敗")) }
            }
        }
    }

    fun sendResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請先輸入 Email")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(email)
                _uiState.update { it.copy(isLoading = false, resetSent = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "發送失敗")) }
            }
        }
    }

    private fun isAllowedEmail(email: String): Boolean = AuthUtils.isAllowedEmail(email)

    fun toggleMode(register: Boolean? = null) {
        val next = register ?: !_uiState.value.isRegisterMode
        _uiState.update { it.copy(
            isRegisterMode = next,
            error = null,
            verificationSent = false,
            verificationSentTo = "",
            verificationLastSentAt = 0L,
        ) }
    }

    fun submit(email: String, password: String, confirmPassword: String, nickname: String, verificationCode: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請填寫 email 和密碼")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("錯誤的信箱格式")) }
            return
        }
        if (_uiState.value.isRegisterMode && password != confirmPassword) {
            _uiState.update { it.copy(error = UiText.Dynamic("兩次密碼不一致")) }
            return
        }
        val pwdError = AuthUtils.validatePassword(password)
        if (pwdError != null) {
            _uiState.update { it.copy(error = UiText.Dynamic(pwdError)) }
            return
        }
        if (_uiState.value.isRegisterMode && nickname.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請填寫暱稱")) }
            return
        }
        if (_uiState.value.isRegisterMode) {
            val validationError = AuthUtils.validateNickname(nickname)
            if (validationError != null) {
                _uiState.update { it.copy(error = UiText.Dynamic(validationError)) }
                return
            }
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                if (_uiState.value.isRegisterMode) {
                    if (verificationCode.isBlank()) {
                        _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("請輸入驗證碼")) }
                        return@launch
                    }
                    val targetEmail = _uiState.value.verificationSentTo.ifBlank { email }
                    val valid = authRepository.verifyVerificationCode(targetEmail, verificationCode)
                    if (!valid) {
                        _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("驗證碼錯誤")) }
                        return@launch
                    }
                    authRepository.register(email, password)
                    val uid = authRepository.currentUserId
                    if (uid.isNotBlank()) {
                        userRepository.setNickname(uid, nickname)
                        authRepository.saveFcmToken()
                    }
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    authRepository.login(email, password)
                    authRepository.saveFcmToken()
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "操作失敗")) }
            }
        }
    }

    fun sendPasswordReset(email: String, verificationCode: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請先輸入 Email")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("錯誤的信箱格式")) }
            return
        }
        if (verificationCode.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請輸入驗證碼")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val targetEmail = _uiState.value.verificationSentTo.ifBlank { email }
                Log.d("AuthViewModel", "sendPasswordReset: targetEmail=$targetEmail code=$verificationCode emailField=$email")
                val valid = authRepository.verifyVerificationCode(targetEmail, verificationCode, prefix = "reset_")
                if (!valid) {
                    _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("驗證碼錯誤")) }
                    return@launch
                }
                _uiState.update { it.copy(isLoading = false, showNewPasswordForm = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "驗證失敗")) }
            }
        }
    }

    fun confirmResetPassword(email: String, verificationCode: String, newPassword: String, confirmNewPassword: String) {
        if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請填寫密碼")) }
            return
        }
        if (newPassword != confirmNewPassword) {
            _uiState.update { it.copy(error = UiText.Dynamic("兩次密碼不一致")) }
            return
        }
        val pwdError = AuthUtils.validatePassword(newPassword)
        if (pwdError != null) {
            _uiState.update { it.copy(error = UiText.Dynamic(pwdError)) }
            return
        }
        if (verificationCode.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("請重新驗證")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "準備重設密碼: email=$email, code=$verificationCode, newPassword=$newPassword")
                authRepository.resetPasswordCloudFunction(email, newPassword, verificationCode)
                _uiState.update { it.copy(isLoading = false, showNewPasswordForm = false) }
                _toastEvent.send(UiText.Dynamic("密碼重設成功，請用新密碼登入"))
                _navigateEvent.send(NavigationEvent.ShowLoginForm)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "密碼重設失敗")) }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        Log.d("AuthViewModel", "signInWithGoogle: idToken=${idToken.take(20)}...")
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
                Log.d("AuthViewModel", "signInWithGoogle: success")
                val uid = authRepository.currentUserId
                val googleName = authRepository.currentUser?.displayName ?: ""
                if (uid.isNotBlank() && googleName.isNotBlank()) {
                    userRepository.getNickname(uid) { existing ->
                        if (existing == "使用者" || existing.isBlank()) {
                            userRepository.setNickname(uid, googleName)
                        }
                    }
                }
                authRepository.saveFcmToken()
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "signInWithGoogle failed: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "登入失敗")) }
            }
        }
    }

    private fun setError(error: UiText) {
        _uiState.update { it.copy(error = error) }
        _toastEvent.trySend(error)
    }

    fun setError(message: String) {
        val error = UiText.Dynamic(message)
        _uiState.update { it.copy(error = error) }
        _toastEvent.trySend(error)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissNewPasswordForm() {
        _uiState.update { it.copy(showNewPasswordForm = false) }
    }

    fun dismissResetSent() {
        _uiState.update { it.copy(resetSent = false) }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    companion object {
        fun validateNickname(nickname: String): String? = AuthUtils.validateNickname(nickname)
    }
}
