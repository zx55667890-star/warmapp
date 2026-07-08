package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class VerifyVerificationCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String, prefix: String = ""): Boolean {
        return authRepository.verifyVerificationCode(email, code, prefix)
    }
}
