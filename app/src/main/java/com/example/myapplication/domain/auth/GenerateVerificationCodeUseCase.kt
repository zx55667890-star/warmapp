package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class GenerateVerificationCodeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, prefix: String = "") {
        authRepository.generateVerificationCode(email, prefix)
    }
}
