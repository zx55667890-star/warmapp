package com.example.myapplication.domain.expert

import com.example.myapplication.data.repository.ExpertRepository

class PublishSkillUseCase(
    private val expertRepository: ExpertRepository
) {
    suspend operator fun invoke(userId: String, expertise: String): String {
        return expertRepository.saveSkill(userId, expertise)
    }
}
