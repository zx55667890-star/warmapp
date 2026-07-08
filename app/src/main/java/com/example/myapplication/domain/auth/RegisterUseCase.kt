package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) {
        authRepository.register(email, password)
    }
}
