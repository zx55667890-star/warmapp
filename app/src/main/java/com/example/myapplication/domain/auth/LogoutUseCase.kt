package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class LogoutUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() {
        authRepository.logout()
    }
}
