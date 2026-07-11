package com.example.myapplication.domain.expert

import com.huaban.analysis.jieba.JiebaSegmenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractLocalTagsUseCase {

    private val segmenter = JiebaSegmenter()
    private val priorityKeywords = setOf("Android", "iOS", "Compose", "淘寶", "台積電")

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.Default) {
        val segments = segmenter.sentenceProcess(text)
        val filteredTags = segments.filter { word ->
            word.length >= 2 && !isStopWord(word)
        }.toMutableSet()

        return@withContext filteredTags.toList()
            .sortedWith(compareByDescending<String> { priorityKeywords.contains(it) }
                .thenByDescending { it.length })
            .take(5)
    }

    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf("的", "了", "和", "從", "到", "在", "是", "嗎", "呢", "啊", "買", "賣")
        return stopWords.contains(word)
    }
}
