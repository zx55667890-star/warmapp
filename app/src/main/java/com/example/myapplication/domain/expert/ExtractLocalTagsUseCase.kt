package com.example.myapplication.domain.expert

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractLocalTagsUseCase {

    private val priorityKeywords = setOf(
        "Android", "iOS", "Compose", "Kotlin", "Java", "Firebase", 
        "淘寶", "蝦皮", "退貨", "物流", "設計", "電腦", "組裝", "程式", "開發"
    )

    private val stopWords = setOf(
        "我", "你", "他", "她", "它", "大家", "自己", "這", "那",
        "會", "教", "讓", "把", "被", "想", "要", "能", "可以", "請問", "幫我", "怎麼", "如何", "做", "用",
        "的", "了", "和", "與", "從", "到", "在", "是", "就", "才", "嗎", "呢", "啊",
        "一個", "一些", "起來", "流程", "教學", "問題", "解決"
    )

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.Default) {
        val resultTags = mutableSetOf<String>()

        val englishOrNumberRegex = Regex("[a-zA-Z0-9]+")
        englishOrNumberRegex.findAll(text).forEach {
            if (it.value.length >= 2) {
                resultTags.add(it.value.lowercase())
            }
        }

        priorityKeywords.forEach { keyword ->
            if (text.contains(keyword, ignoreCase = true)) {
                resultTags.add(keyword)
            }
        }

        val pureChineseChunks = text.replace(Regex("[^\\p{IsHan}]+"), " ").split(" ")

        pureChineseChunks.filter { it.length >= 2 }.forEach { chunk ->
            val bigrams = chunk.windowed(2)
            bigrams.forEach { word ->
                val hasStopWord = stopWords.any { stopWord -> word.contains(stopWord) }
                if (!hasStopWord) {
                    resultTags.add(word)
                }
            }
        }

        return@withContext resultTags.toList()
            .sortedByDescending { it.length }
            .take(4)
    }
}
