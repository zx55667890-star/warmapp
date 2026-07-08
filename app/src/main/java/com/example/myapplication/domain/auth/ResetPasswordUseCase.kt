package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend fun sendResetEmail(email: String) {
        authRepository.sendPasswordReset(email)
    }

    suspend fun resetViaCloudFunction(email: String, newPassword: String, code: String) {
        authRepository.resetPasswordCloudFunction(email, newPassword, code)
    }

    fun markResetVerified(email: String) {
        authRepository.markResetVerified(email)
    }

    suspend fun generateResetCode(email: String) {
        authRepository.generateVerificationCode(email, "reset_")
    }

    suspend fun verifyResetCode(email: String, code: String): Boolean {
        return authRepository.verifyVerificationCode(email, code, "reset_")
    }
}
