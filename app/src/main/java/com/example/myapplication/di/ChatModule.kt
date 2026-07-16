package com.example.myapplication.di

import com.example.myapplication.domain.chat.FetchOpponentUseCase
import com.example.myapplication.domain.chat.ObserveMessagesUseCase
import com.example.myapplication.domain.chat.RecallMessageUseCase
import com.example.myapplication.domain.chat.SendMediaUseCase
import com.example.myapplication.domain.chat.SendTextMessageUseCase
import com.example.myapplication.ui.chat.ChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single { SendTextMessageUseCase(get()) }
    single { SendMediaUseCase(get(), get()) }
    single { RecallMessageUseCase(get(), get()) }
    single { ObserveMessagesUseCase(get()) }
    single { FetchOpponentUseCase(get(), get()) }

    viewModel { ChatViewModel(get(), get(), get(), get(), get()) }
}
