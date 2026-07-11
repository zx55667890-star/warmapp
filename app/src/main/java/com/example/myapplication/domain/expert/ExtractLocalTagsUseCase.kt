package com.example.myapplication.domain.expert

import com.example.myapplication.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractLocalTagsUseCase {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend operator fun invoke(text: String): List<String> = withContext(Dispatchers.IO) {
        val prompt = """
            請從以下文字中提取 4 個最核心的關鍵字標籤。
            要求：
            1. 只回傳標籤內容，用逗號分隔 (例如：台積電,股票,投資,理財)。
            2. 不要包含任何解釋、序號或符號。
            3. 處理「台積電」、「比特幣」等熱門專有名詞時，請務必完整保留，不要拆開。
            
            文字內容：$text
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            response.text?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.take(4) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
