package com.example.myapplication.domain.expert

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractLocalTagsUseCase {

    private val priorityKeywords = setOf(
        "Android", "iOS", "Compose", "Kotlin", "Java", "Firebase",
        "淘寶", "蝦皮", "退貨", "物流", "設計", "電腦", "組裝", "程式", "開發",
        "寸頭", "臉型", "髮型", "男生", "女生"
    )

    private val stopWords = setOf(
        "大家", "自己", "請問", "幫我", "怎麼", "如何", "一個", "一些", "起來", "流程", "教學", "問題", "解決",
        "我", "你", "他", "她", "它", "這", "那",
        "會", "教", "讓", "把", "被", "想", "要", "能", "可以", "做", "用",
        "的", "了", "和", "與", "從", "到", "在", "是", "就", "才", "嗎", "呢", "啊",
        "適合", "專門", "需要", "推薦"
    )

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.Default) {
        val resultTags = LinkedHashSet<String>()
        var workingText = text

        val englishOrNumberRegex = Regex("[a-zA-Z0-9]+")
        englishOrNumberRegex.findAll(workingText).forEach {
            if (it.value.length >= 2) {
                resultTags.add(it.value.lowercase())
            }
        }
        workingText = workingText.replace(englishOrNumberRegex, " ")

        priorityKeywords.forEach { keyword ->
            if (workingText.contains(keyword, ignoreCase = true)) {
                resultTags.add(keyword)
                workingText = workingText.replace(keyword, " ", ignoreCase = true)
            }
        }

        val sortedStopWords = stopWords.sortedByDescending { it.length }
        sortedStopWords.forEach { stopWord ->
            workingText = workingText.replace(stopWord, " ")
        }

        workingText = workingText.replace(Regex("[^\u4e00-\u9fa5]+"), " ")

        val perfectChunks = mutableListOf<String>()
        val guessedBigrams = mutableListOf<String>()

        val chunks = workingText.split(" ").filter { it.isNotBlank() }
        chunks.forEach { chunk ->
            if (chunk.length == 2) {
                perfectChunks.add(chunk)
            } else if (chunk.length > 2) {
                chunk.windowed(2).forEach { guessedBigrams.add(it) }
            }
        }

        return@withContext (resultTags.toList() + perfectChunks + guessedBigrams)
            .distinct()
            .take(5)
    }
}
