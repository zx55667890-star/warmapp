package com.example.myapplication.di

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.expert.ExtractLocalTagsUseCase
import kotlinx.coroutines.launch

class TagViewModel(sharedPrefs: SharedPreferences) : ViewModel() {
    private val extractLocalTagsUseCase = ExtractLocalTagsUseCase(sharedPrefs)

    fun extractTags(text: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val tags = extractLocalTagsUseCase(text)
            Log.d("TagExtract", "輸入文字: $text")
            Log.d("TagExtract", "產出標籤: $tags")
            onResult(tags)
        }
    }
}
