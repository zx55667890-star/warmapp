package com.example.myapplication.domain.seeker

import com.example.myapplication.data.repository.QuestionRepository

sealed class QuotaResult {
    data object Valid : QuotaResult()
    data class Invalid(val reason: String) : QuotaResult()
}

class ValidateQuestionQuotaUseCase(
    private val questionRepository: QuestionRepository
) {
    companion object {
        const val DAILY_LIMIT = 3
    }

    suspend operator fun invoke(userId: String): QuotaResult {
        val hasActive = questionRepository.hasActiveQuestion(userId)
        if (hasActive) {
            return QuotaResult.Invalid("您當前已有進行中或媒合中的提問，請先完成或取消該對話！")
        }

        val todayCount = questionRepository.getTodayQuestionCount(userId)
        if (todayCount >= DAILY_LIMIT) {
            return QuotaResult.Invalid("已達今日提問上限（每日最多 $DAILY_LIMIT 次）！")
        }

        return QuotaResult.Valid
    }
}
