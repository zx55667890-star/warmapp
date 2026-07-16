package com.example.myapplication.di

import com.example.myapplication.data.repository.MatchingRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.domain.seeker.ObserveQuestionStatusUseCase
import com.example.myapplication.domain.seeker.SendQuestionMediaUseCase
import com.example.myapplication.domain.seeker.ValidateQuestionQuotaUseCase
import com.example.myapplication.ui.seeker.SeekerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val seekerModule = module {
    single<MatchingRepositoryInterface> { MatchingRepository(get()) }
    single { QuestionRepository(get()) }
    single { ValidateQuestionQuotaUseCase(get()) }
    single { ObserveQuestionStatusUseCase(get()) }
    single { SendQuestionMediaUseCase(get()) }

    viewModel {
        SeekerViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get()
        )
    }
}
