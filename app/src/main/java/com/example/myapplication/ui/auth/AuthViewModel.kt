package com.example.myapplication.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.domain.auth.GenerateVerificationCodeUseCase
import com.example.myapplication.domain.auth.LoginUseCase
import com.example.myapplication.domain.auth.LogoutUseCase
import com.example.myapplication.domain.auth.RegisterUseCase
import com.example.myapplication.domain.auth.ResetPasswordUseCase
import com.example.myapplication.domain.auth.SignInWithGoogleUseCase
import com.example.myapplication.domain.auth.VerifyVerificationCodeUseCase
import com.example.myapplication.ui.common.AuthUtils
import com.example.myapplication.ui.common.UiText
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val generateVerificationCodeUseCase: GenerateVerificationCodeUseCase,
    private val verifyVerificationCodeUseCase: VerifyVerificationCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val logoutUseCase: LogoutUseCase,
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
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å…ˆè¼¸å…¥ Email")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("éŒ¯èª¤çš„ä¿¡ç®±æ ¼å¼")) }
            return
        }
        val now = System.currentTimeMillis()
        val isReset = prefix == "reset_"
        val lastSent = if (isReset) _uiState.value.resetVerificationLastSentAt else _uiState.value.verificationLastSentAt
        if (now - lastSent < 60_000L) {
            val remain = 60 - (now - lastSent) / 1000
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹ ${remain} ç§’å¾Œå†è©¦")) }
            return
        }
        viewModelScope.launch {
            try {
                generateVerificationCodeUseCase(email, prefix = prefix)
                _uiState.update {
                    it.copy(
                        error = null, verificationSent = true,
                        verificationSentTo = email,
                        verificationLastSentAt = if (!isReset) now else it.verificationLastSentAt,
                        resetVerificationLastSentAt = if (isReset) now else it.resetVerificationLastSentAt
                    )
                }
                _toastEvent.send(UiText.Dynamic("é©—è­‰ç¢¼å·²ç™¼é€è‡³ $email"))
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                _uiState.update { it.copy(error = UiText.Dynamic(e.message ?: "ç™¼é€å¤±æ•—")) }
            }
        }
    }

    fun sendResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å…ˆè¼¸å…¥ Email")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                resetPasswordUseCase.sendResetEmail(email)
                _uiState.update { it.copy(isLoading = false, resetSent = true) }
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "ç™¼é€å¤±æ•—")) }
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
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å¡«å¯« email å’Œå¯†ç¢¼")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("éŒ¯èª¤çš„ä¿¡ç®±æ ¼å¼")) }
            return
        }
        if (_uiState.value.isRegisterMode && password != confirmPassword) {
            _uiState.update { it.copy(error = UiText.Dynamic("å…©æ¬¡å¯†ç¢¼ä¸ä¸€è‡´")) }
            return
        }
        val pwdError = AuthUtils.validatePassword(password)
        if (pwdError != null) {
            _uiState.update { it.copy(error = UiText.Dynamic(pwdError)) }
            return
        }
        if (_uiState.value.isRegisterMode && nickname.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å¡«å¯«æš±ç¨±")) }
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
                        _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("è«‹è¼¸å…¥é©—è­‰ç¢¼")) }
                        return@launch
                    }
                    val targetEmail = _uiState.value.verificationSentTo.ifBlank { email }
                    val valid = verifyVerificationCodeUseCase(targetEmail, verificationCode)
                    if (!valid) {
                        _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("é©—è­‰ç¢¼éŒ¯èª¤")) }
                        return@launch
                    }
                    registerUseCase(email, password)
                    val uid = authRepository.currentUserId
                    if (uid.isNotBlank()) {
                        userRepository.setNickname(uid, nickname)
                        authRepository.saveFcmToken()
                    }
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    loginUseCase(email, password)
                    authRepository.saveFcmToken()
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "æ“ä½œå¤±æ•—")) }
            }
        }
    }

    fun sendPasswordReset(email: String, verificationCode: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å…ˆè¼¸å…¥ Email")) }
            return
        }
        if (!isAllowedEmail(email)) {
            _uiState.update { it.copy(error = UiText.Dynamic("éŒ¯èª¤çš„ä¿¡ç®±æ ¼å¼")) }
            return
        }
        if (verificationCode.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹è¼¸å…¥é©—è­‰ç¢¼")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val targetEmail = _uiState.value.verificationSentTo.ifBlank { email }
                Log.d("AuthViewModel", "sendPasswordReset: targetEmail=$targetEmail code=$verificationCode emailField=$email")
                val valid = resetPasswordUseCase.verifyResetCode(targetEmail, verificationCode)
                if (!valid) {
                    _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("é©—è­‰ç¢¼éŒ¯èª¤")) }
                    return@launch
                }
                _uiState.update { it.copy(isLoading = false, showNewPasswordForm = true) }
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "é©—è­‰å¤±æ•—")) }
            }
        }
    }

    fun confirmResetPassword(email: String, verificationCode: String, newPassword: String, confirmNewPassword: String) {
        if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹å¡«å¯«å¯†ç¢¼")) }
            return
        }
        if (newPassword != confirmNewPassword) {
            _uiState.update { it.copy(error = UiText.Dynamic("å…©æ¬¡å¯†ç¢¼ä¸ä¸€è‡´")) }
            return
        }
        val pwdError = AuthUtils.validatePassword(newPassword)
        if (pwdError != null) {
            _uiState.update { it.copy(error = UiText.Dynamic(pwdError)) }
            return
        }
        if (verificationCode.isBlank()) {
            _uiState.update { it.copy(error = UiText.Dynamic("è«‹é‡æ–°é©—è­‰")) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "æº–å‚™é‡è¨­å¯†ç¢¼: email=$email, code=$verificationCode, newPassword=$newPassword")
                resetPasswordUseCase.resetViaCloudFunction(email, newPassword, verificationCode)
                _uiState.update { it.copy(isLoading = false, showNewPasswordForm = false) }
                _toastEvent.send(UiText.Dynamic("å¯†ç¢¼é‡è¨­æˆåŠŸï¼Œè«‹ç”¨æ–°å¯†ç¢¼ç™»å…¥"))
                _navigateEvent.send(NavigationEvent.ShowLoginForm)
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "å¯†ç¢¼é‡è¨­å¤±æ•—")) }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        Log.d("AuthViewModel", "signInWithGoogle: idToken=${idToken.take(20)}...")
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                withTimeout(20_000L) {
                    signInWithGoogleUseCase(idToken)
                }
                Log.d("AuthViewModel", "signInWithGoogle: success")
                val uid = authRepository.currentUserId
                val googleName = authRepository.currentUser?.displayName ?: ""
                if (uid.isNotBlank() && googleName.isNotBlank()) {
                    val existing = userRepository.getNickname(uid)
                    if (existing == "使用者" || existing.isBlank()) {
                        userRepository.setNickname(uid, googleName)
                    }
                }
                authRepository.saveFcmToken()
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: TimeoutCancellationException) {
                Log.w("AuthViewModel", "signInWithGoogle timeout")
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic("ç™»å…¥é€¾æ™‚ï¼Œè«‹æª¢æŸ¥ç¶²è·¯é€£ç·šæˆ– Firebase Google ç™»å…¥æ˜¯å¦é–‹å•Ÿ")) }
            } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                Log.w("AuthViewModel", "signInWithGoogle failed: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = UiText.Dynamic(e.message ?: "ç™»å…¥å¤±æ•—")) }
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
        logoutUseCase()
        _uiState.value = AuthUiState()
    }

    companion object {
        fun validateNickname(nickname: String): String? = AuthUtils.validateNickname(nickname)
    }
}

