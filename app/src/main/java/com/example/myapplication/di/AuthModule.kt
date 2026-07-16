package com.example.myapplication.di

import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.domain.auth.GenerateVerificationCodeUseCase
import com.example.myapplication.domain.auth.LoginUseCase
import com.example.myapplication.domain.auth.LogoutUseCase
import com.example.myapplication.domain.auth.RegisterUseCase
import com.example.myapplication.domain.auth.ResetPasswordUseCase
import com.example.myapplication.domain.auth.SignInWithGoogleUseCase
import com.example.myapplication.domain.auth.VerifyVerificationCodeUseCase
import com.example.myapplication.ui.auth.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthRepository(get(), androidContext(), get()) }
    single { LoginUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { SignInWithGoogleUseCase(get()) }
    single { GenerateVerificationCodeUseCase(get()) }
    single { VerifyVerificationCodeUseCase(get()) }
    single { ResetPasswordUseCase(get()) }
    single { LogoutUseCase(get()) }

    viewModel {
        AuthViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
