package com.example.myapplication.di

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.expert.ExtractLocalTagsUseCase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class TagViewModel(sharedPrefs: SharedPreferences, firebaseDb: FirebaseDatabase) : ViewModel() {
    private val extractLocalTagsUseCase = ExtractLocalTagsUseCase(sharedPrefs, firebaseDb)

    fun extractTags(text: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            val tags = extractLocalTagsUseCase(text)
            Log.d("TagExtract", "輸入文字: $text")
            Log.d("TagExtract", "產出標籤: $tags")
            onResult(tags)
        }
    }
}
