package com.example.myapplication.di

import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.ExpertRepository
import com.example.myapplication.domain.expert.ObserveSolutionsUseCase
import com.example.myapplication.domain.expert.PublishSkillUseCase
import com.example.myapplication.ui.expert.ExpertViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val expertModule = module {
    single { ExpertRepository(get()) }
    single { AiRepository(get()) }
    single { PublishSkillUseCase(get()) }
    single { ObserveSolutionsUseCase(get()) }

    viewModel { ExpertViewModel(get(), get(), get(), get()) }
}
