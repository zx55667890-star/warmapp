package com.example.myapplication.data.model

enum class SkillStatus {
    ACTIVE, PENDING, REJECTED
}

data class SolutionItem(
    val id: String = "",
    val questionId: String = "",
    val expertise: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val status: String = SkillStatus.ACTIVE.name
)
