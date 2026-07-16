package com.example.myapplication.di

import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.ui.camera.CameraViewModel
import com.example.myapplication.ui.voice.VoiceRecordingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    single { MediaUploader(get()) }
    viewModel { CameraViewModel() }
    viewModel { VoiceRecordingViewModel() }
}
