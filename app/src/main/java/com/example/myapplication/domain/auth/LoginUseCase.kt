package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) {
        authRepository.login(email, password)
    }
}
