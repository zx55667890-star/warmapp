package com.example.myapplication.domain.expert

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractLocalTagsUseCase {

    private val priorityKeywords = setOf(
        "Android", "iOS", "Compose", "Kotlin", "Java", "Koin", "Firebase",
        "淘寶", "蝦皮", "退貨", "物流", "海運", "空運", "報關"
    )

    private val stopWords = setOf(
        "的", "了", "和", "與", "從", "到", "在", "是", "我", "你", "他", "這", "那",
        "可以", "怎麼", "如何", "幫我", "請問", "流程", "教學", "問題", "解決", "一個"
    )

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.Default) {
        val resultTags = mutableSetOf<String>()
        val cleanText = text.replace(Regex("[\\p{Punct}\\s，。！？、；：()（）「」]"), "")

        priorityKeywords.forEach { keyword ->
            if (cleanText.contains(keyword, ignoreCase = true)) {
                resultTags.add(keyword)
            }
        }

        if (cleanText.length >= 2) {
            val bigrams = cleanText.windowed(2)
            bigrams.forEach { word ->
                if (!word.all { it.isDigit() } && !containsStopWord(word)) {
                    resultTags.add(word)
                }
            }
        }

        return@withContext resultTags.toList()
            .sortedByDescending { it.length }
            .take(4)
    }

    private fun containsStopWord(word: String): Boolean {
        return stopWords.any { word.contains(it) }
    }
}
