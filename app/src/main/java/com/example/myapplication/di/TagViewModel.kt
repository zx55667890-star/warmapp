package com.example.myapplication.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.expert.ExtractLocalTagsUseCase
import kotlinx.coroutines.launch

class TagViewModel : ViewModel() {
    private val extractLocalTagsUseCase = ExtractLocalTagsUseCase()

    fun extractTags(text: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val tags = extractLocalTagsUseCase(text)
            onResult(tags)
        }
    }
}
