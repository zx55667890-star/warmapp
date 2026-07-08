package com.example.myapplication.domain.auth

import com.example.myapplication.data.repository.AuthRepository

class SignInWithGoogleUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String) {
        authRepository.signInWithGoogle(idToken)
    }
}
