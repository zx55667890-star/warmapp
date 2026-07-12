package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.DataMigrator
import com.example.myapplication.data.repository.ExpertRepository
import com.example.myapplication.data.repository.MatchingRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.MessageRepositoryFactory
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.domain.auth.GenerateVerificationCodeUseCase
import com.example.myapplication.domain.auth.LoginUseCase
import com.example.myapplication.domain.auth.LogoutUseCase
import com.example.myapplication.domain.auth.RegisterUseCase
import com.example.myapplication.domain.auth.ResetPasswordUseCase
import com.example.myapplication.domain.auth.SignInWithGoogleUseCase
import com.example.myapplication.domain.auth.VerifyVerificationCodeUseCase
import com.example.myapplication.domain.chat.ObserveMessagesUseCase
import com.example.myapplication.domain.chat.RecallMessageUseCase
import com.example.myapplication.domain.chat.SendMediaUseCase
import com.example.myapplication.domain.chat.FetchOpponentUseCase
import com.example.myapplication.domain.chat.SendTextMessageUseCase
import com.example.myapplication.domain.seeker.ObserveQuestionStatusUseCase
import com.example.myapplication.domain.seeker.SendQuestionMediaUseCase
import com.example.myapplication.domain.seeker.ValidateQuestionQuotaUseCase
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.chat.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
            getReference("questions").keepSynced(true)
            getReference("experts").keepSynced(true)
            getReference("experiences").keepSynced(true)
            getReference("users").keepSynced(true)
        }
    }
    single { FirebaseStorage.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { AuthRepository(get(), androidContext(), get()) }
    single {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    single { ExpertRepository(get()) }
    single<MatchingRepositoryInterface> { MatchingRepository(get()) }
    single { QuestionRepository(get()) }
    single { MediaUploader(get()) }
    single { DataMigrator(get(), get()) }
    single { UserRepository(get()) }
    single { AiRepository(get()) }

    single { MessageRepositoryFactory(get()) }
    single { SendTextMessageUseCase(get()) }
    single { SendMediaUseCase(get(), get()) }
    single { RecallMessageUseCase(get(), get()) }
    single { ObserveMessagesUseCase(get()) }
    single { FetchOpponentUseCase(get(), get()) }

    single { LoginUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { SignInWithGoogleUseCase(get()) }
    single { GenerateVerificationCodeUseCase(get()) }
    single { VerifyVerificationCodeUseCase(get()) }
    single { ResetPasswordUseCase(get()) }
    single { LogoutUseCase(get()) }

    factory { ValidateQuestionQuotaUseCase(get()) }
    factory { ObserveQuestionStatusUseCase(get()) }
    factory { SendQuestionMediaUseCase(get()) }

    viewModel { AuthViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ExpertViewModel(get(), get()) }
    viewModel { SeekerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ChatViewModel(get(), get(), get(), get(), get()) }
}
