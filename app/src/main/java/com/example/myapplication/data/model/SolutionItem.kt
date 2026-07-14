package com.example.myapplication.data.model

enum class SkillStatus {
    ACTIVE, PENDING, REJECTED;

    companion object {
        fun fromName(name: String?): SkillStatus =
            entries.firstOrNull { it.name == name } ?: ACTIVE
    }
}

data class SolutionItem(
    val id: String = "",
    val questionId: String = "",
    val expertise: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val status: SkillStatus = SkillStatus.ACTIVE
)
